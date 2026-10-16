#!/usr/bin/env python3
"""
Builds the v2 import files for the data seeded in v1 (Othelose, AOE2 / Wonder Empires Football).

Input:  a folder with one JSON array per legacy_v1 table (SELECT json_agg(t) FROM legacy_v1.<table> t),
        exported from database competition_fantasy, schema legacy_v1.
Output: imports/legacy-v1/othelose.json and imports/legacy-v1/aoe2.json (schema imports/schema/v1.json).

    python3 tools/convert_legacy_v1.py <export-dir> imports/legacy-v1

Decisions (agreed for this migration):
- Two universes as in v1. The AOE2 World Cup 2026 is not carried over.
- Notebook (RECORDED) tables are ranked the way v1 showed them: points, then goals for (v1's query order;
  rows level on both keep their stored order). v1 CALCULATED tables are not copied (v2 recalculates them).
- Wonder Empires Season 1 has no dates: v1's generated dates overlapped across stages.
- v1 short_name becomes the team code; Othelose club colours become a team profile for every year the club played.
- Mapuche, Muisca and Tupi get the code and colours from v1's aoe2_rebuild_foundation.sql (missing in the v1 database).
- Nation images reference the files already on the CDN: v1 flag_url -> FLAG, v1 logo_url -> EMBLEM.
"""
import collections
import json
import os
import re
import sys
import unicodedata

UNIVERSES = {
    "Othelose": {"key": "othelose", "file": "othelose.json", "usesNations": False},
    "Age Of Empires 2 Universe": {"key": "aoe2", "file": "aoe2.json", "usesNations": True},
}
COMPETITIONS = {
    "Othelose League": {"key": "othelose-league", "tier": 1, "preset": "LEAGUE_2RR"},
    "Wonder Empires Football": {"key": "wonder-empires-football", "tier": None, "preset": "WONDER_EMPIRES_C1"},
}
SKIP_SEASONS = {"World Cup 2026"}
SEASONS_WITHOUT_DATES = {("Wonder Empires Football", "Season 1")}
# from v1 competition-api/src/main/resources/scripts/aoe2_rebuild_foundation.sql
MISSING_NATION_FIELDS = {
    "Mapuche": ("MAP", "#1B4F9C", "#FFFFFF", "#FFFFFF", "#1B4F9C"),
    "Muisca": ("MUI", "#C9A227", "#222222", "#222222", "#C9A227"),
    "Tupi": ("TUP", "#1E7A3D", "#FFD23F", "#FFD23F", "#1E7A3D"),
}
# v1 knockout stages (one stage per round) -> one v2 knockout stage, keys from the WONDER_EMPIRES_C1 preset
KO_ROUND_KEYS = {"Quarter Final": "QF", "Semi Final": "SF", "Final": "F"}
GROUP_STAGE_KEYS = {"Group Stage": "GS", "Elimination Groups II": "EG", "League": "REG"}


def slug(text):
    s = unicodedata.normalize("NFKD", text).encode("ascii", "ignore").decode().lower()
    return re.sub(r"[^a-z0-9]+", "-", s).strip("-")[:64]


def compact(d):
    """Drops None values and empty nested objects."""
    out = {}
    for k, v in d.items():
        if isinstance(v, dict):
            v = compact(v)
            if not v:
                continue
        if v is not None:
            out[k] = v
    return out


def day(ts):
    return ts[:10] if ts else None


class V1:
    def __init__(self, folder):
        load = lambda t: json.load(open(os.path.join(folder, t + ".json"), encoding="utf-8"))
        self.universes = load("universes")
        self.nations = {n["id"]: n for n in load("nations")}
        self.teams = {t["id"]: t for t in load("teams")}
        self.competitions = load("competitions")
        self.seasons = load("seasons")
        self.stages = load("stages")
        self.groups = load("stage_groups")
        self.group_teams = load("stage_group_teams")
        self.season_teams = load("season_teams")
        self.rounds = {r["id"]: r for r in load("rounds")}
        self.matches = load("matches")
        self.standings = load("standings")  # array order = order rows were entered (notebook order)
        self.media = {m["id"]: m for m in load("media_files")}


def image(v1, url, media_id):
    if not url:
        return None
    m = v1.media.get(media_id) or {}
    return compact({"url": url, "contentType": m.get("content_type"), "width": m.get("width"),
                    "height": m.get("height"), "sizeBytes": m.get("file_size")})


def build_universe(v1, u, cfg, keys):
    uid = u["id"]
    doc = {"schemaVersion": 1,
           "universe": compact({"key": cfg["key"], "name": u["name"], "description": u["description"] or None,
                                "type": "FICTIONAL", "usesNations": cfg["usesNations"]})}

    nations = sorted((n for n in v1.nations.values() if n["universe_id"] == uid), key=lambda n: n["name"])
    nation_key = {}
    if nations:
        doc["nations"] = []
        for n in nations:
            code, primary, text, away, away_text = n["code"], n["primary_color"], n["text_color"], n["away_color"], n["away_text_color"]
            if not code and n["name"] in MISSING_NATION_FIELDS:
                code, primary, text, away, away_text = MISSING_NATION_FIELDS[n["name"]]
            nation_key[n["id"]] = slug(n["name"])
            doc["nations"].append(compact({
                "key": nation_key[n["id"]], "name": n["name"], "code": code,
                "colors": {"primary": primary, "text": text, "away": {"bg": away, "text": away_text}},
                "description": n["description"] or None,
                "images": {"FLAG": image(v1, n["flag_url"], n["flag_media_id"]), "EMBLEM": image(v1, n["logo_url"], n["logo_media_id"])},
            }))

    teams = sorted((t for t in v1.teams.values() if t["universe_id"] == uid), key=lambda t: (t["type"] != "NATIONAL", t["name"]))
    doc["teams"] = []
    for t in teams:
        keys[t["id"]] = slug(t["name"])
        code = t["short_name"]
        if not code and t["type"] == "NATIONAL" and t["nation_id"] in v1.nations:
            code = MISSING_NATION_FIELDS.get(v1.nations[t["nation_id"]]["name"], (None,))[0]
        doc["teams"].append(compact({
            "key": keys[t["id"]], "name": t["name"], "code": code, "type": t["type"],
            "nation": nation_key.get(t["nation_id"]), "description": t["description"] or None,
        }))
    if len(set(keys[t["id"]] for t in teams)) != len(teams):
        raise SystemExit("team keys are not unique in " + u["name"])

    comps = [c for c in v1.competitions if c["universe_id"] == uid and c["name"] in COMPETITIONS]
    seasons = [s for s in v1.seasons if any(s["competition_id"] == c["id"] for c in comps) and s["name"] not in SKIP_SEASONS]

    # one profile per club and year played, for clubs that have their own colours in v1
    years = collections.defaultdict(set)
    season_year = {s["id"]: s["year"] for s in seasons}
    for st in v1.season_teams:
        if st["season_id"] in season_year:
            years[st["team_id"]].add(season_year[st["season_id"]])
    profiles = []
    for t in teams:
        colours = compact({"primary": t["primary_color"], "secondary": t["secondary_color"],
                           "home": {"bg": t["home_bg"], "text": t["home_text"]}, "away": {"bg": t["away_bg"], "text": t["away_text"]}})
        if colours:
            for y in sorted(years[t["id"]]):
                profiles.append({"team": keys[t["id"]], "year": y, "colors": colours})
    if profiles:
        doc["teamProfiles"] = profiles

    doc["competitions"] = [compact({"key": COMPETITIONS[c["name"]]["key"], "name": c["name"], "sport": "FOOTBALL",
                                    "teamLevel": c["team_level"], "tier": COMPETITIONS[c["name"]]["tier"],
                                    "description": c["description"] or None}) for c in comps]
    doc["seasons"] = [build_season(v1, s, next(c for c in comps if c["id"] == s["competition_id"]), keys)
                      for s in sorted(seasons, key=lambda s: (s["competition_id"], s["year"] or 0, s["name"]))]
    return doc


def match_in(m, keys, leg=None, with_date=True):
    if m["status"] != "COMPLETED" or m["home_score"] is None:
        raise SystemExit("unexpected unplayed match " + m["id"])
    return compact({"leg": leg, "home": keys[m["home_team_id"]], "away": keys[m["away_team_id"]],
                    "homeScore": m["home_score"], "awayScore": m["away_score"],
                    "homePens": m["home_penalties"], "awayPens": m["away_penalties"],
                    "date": day(m["match_date"]) if with_date else None,
                    "status": "PLAYED", "source": "NOTEBOOK", "confidence": "VERIFIED", "notes": m["notes"] or None})


def recorded_tables(v1, season_id, group_id, keys):
    tables = collections.OrderedDict()
    for x in v1.standings:
        if x["type"] == "RECORDED" and x["season_id"] == season_id and x["stage_group_id"] == group_id:
            tables.setdefault(x["after_round"] or 0, []).append(x)
    out = []
    for after in sorted(tables, key=lambda a: (a == 0, a)):  # checkpoints in order, final table last
        ranked = sorted(tables[after], key=lambda r: (-r["points"], -r["goals_for"]))  # v1: ORDER BY points DESC, goals_for DESC
        out.append({"afterRound": after, "rows": [
            {"team": keys[r["team_id"]], "position": i + 1, "played": r["played"], "won": r["won"], "drawn": r["drawn"],
             "lost": r["lost"], "goalsFor": r["goals_for"], "goalsAgainst": r["goals_against"], "points": r["points"]}
            for i, r in enumerate(ranked)]})
    return out


def build_season(v1, s, comp, keys):
    cfg = COMPETITIONS[comp["name"]]
    with_dates = (comp["name"], s["name"]) not in SEASONS_WITHOUT_DATES
    stages = sorted((x for x in v1.stages if x["season_id"] == s["id"]), key=lambda x: x["order_number"])
    by_round = collections.defaultdict(list)
    for m in v1.matches:
        by_round[m["round_id"]].append(m)
    all_matches = [m for r in v1.rounds.values() if r["season_id"] == s["id"] for m in by_round[r["id"]]]
    dates = sorted(day(m["match_date"]) for m in all_matches if m["match_date"])

    out = {"competition": cfg["key"], "key": slug(s["name"]), "name": s["name"], "year": s["year"],
           "startDate": dates[0] if with_dates and dates else None, "endDate": dates[-1] if with_dates and dates else None,
           "status": s["status"], "preset": cfg["preset"],
           "teams": [{"team": keys[st["team_id"]]} for st in v1.season_teams if st["season_id"] == s["id"]],
           "stages": [], "honours": []}

    ko_rounds = []
    for st in stages:
        if st["type"] == "GROUP":
            stage = {"key": GROUP_STAGE_KEYS[st["name"]], "name": st["name"], "type": "ROUND_ROBIN", "groups": []}
            for g in sorted((g for g in v1.groups if g["stage_id"] == st["id"]), key=lambda g: g["name"]):
                letter = g["name"].replace("Group", "").strip() or "A"
                rounds = sorted((r for r in v1.rounds.values() if r["stage_group_id"] == g["id"]), key=lambda r: r["round_number"])
                rounds_in = []
                for r in rounds:
                    ms = sorted(by_round[r["id"]], key=lambda m: (m["match_date"] or "", keys[m["home_team_id"]]))
                    rd = sorted(day(m["match_date"]) for m in ms if m["match_date"])
                    rounds_in.append(compact({"number": r["round_number"], "name": r["name"],
                                              "startDate": rd[0] if with_dates and rd else None,
                                              "endDate": rd[-1] if with_dates and rd else None}) |
                                     {"matches": [match_in(m, keys, with_date=with_dates) for m in ms]})
                stage["groups"].append({
                    "key": letter if letter != "League" else "A", "name": g["name"],
                    "teams": [keys[x["team_id"]] for x in v1.group_teams if x["stage_group_id"] == g["id"]],
                    "rounds": rounds_in,
                    "recordedStandings": recorded_tables(v1, s["id"], g["id"], keys)})
            out["stages"].append(stage)
        else:
            ko_rounds.append(st)

    if ko_rounds:
        rounds_out = []
        for st in ko_rounds:
            legs = st["legs"] or 1
            ms = sorted((m for r in v1.rounds.values() if r["stage_id"] == st["id"] for m in by_round[r["id"]]),
                        key=lambda m: (v1.rounds[m["round_id"]]["round_number"], m["match_date"] or ""))
            pairs = collections.OrderedDict()
            for m in ms:
                pairs.setdefault(frozenset((m["home_team_id"], m["away_team_id"])), []).append(m)
            ties = []
            for legs_list in pairs.values():
                first = legs_list[0]
                goals = collections.Counter()
                for m in legs_list:
                    goals[m["home_team_id"]] += m["home_score"]
                    goals[m["away_team_id"]] += m["away_score"]
                h, a = first["home_team_id"], first["away_team_id"]
                if goals[h] == goals[a]:
                    raise SystemExit("level aggregate needs a decider: " + st["name"])
                ties.append({"homeId": h, "awayId": a, "winnerId": h if goals[h] > goals[a] else a,
                             "matches": [match_in(m, keys, leg=i + 1 if legs > 1 else None, with_date=with_dates)
                                         for i, m in enumerate(legs_list)]})
            rounds_out.append({"key": KO_ROUND_KEYS[st["name"]], "name": st["name"], "legs": legs, "ties": ties})
        # order each round's ties by the tie their winner plays next, so the bracket reads straight across
        for i in range(len(rounds_out) - 2, -1, -1):
            nxt = rounds_out[i + 1]["ties"]
            slot = {}
            for j, t in enumerate(nxt):
                slot[t["homeId"]] = (j, 0)
                slot[t["awayId"]] = (j, 1)
            rounds_out[i]["ties"].sort(key=lambda t: slot.get(t["winnerId"], (99, 0)))
        out["stages"].append({"key": "KO", "name": "Knockout stage", "type": "KNOCKOUT", "rounds": [
            {"key": r["key"], "name": r["name"], "legs": r["legs"], "ties": [
                {"position": j + 1, "home": keys[t["homeId"]], "away": keys[t["awayId"]], "matches": t["matches"]}
                for j, t in enumerate(r["ties"])]} for r in rounds_out]})
        final = rounds_out[-1]["ties"][0]
        loser = final["awayId"] if final["winnerId"] == final["homeId"] else final["homeId"]
        out["honours"] = [{"team": keys[final["winnerId"]], "kind": "CHAMPION"}, {"team": keys[loser], "kind": "RUNNER_UP"}]
    else:
        table = out["stages"][0]["groups"][0]["recordedStandings"][-1]  # final notebook table
        assert table["afterRound"] == 0
        out["honours"] = [{"team": table["rows"][0]["team"], "kind": "CHAMPION"}, {"team": table["rows"][1]["team"], "kind": "RUNNER_UP"}]
    return compact(out) | {"teams": out["teams"], "stages": out["stages"], "honours": out["honours"]}


def dumps(value, indent=0):
    """Pretty JSON where flat objects (a match, a table row) stay on one line."""
    pad, inner = "  " * indent, "  " * (indent + 1)
    if isinstance(value, dict):
        if not value:
            return "{}"
        if all(not isinstance(v, (dict, list)) for v in value.values()):
            return "{ " + ", ".join(json.dumps(k) + ": " + json.dumps(v, ensure_ascii=False) for k, v in value.items()) + " }"
        return "{\n" + ",\n".join(inner + json.dumps(k) + ": " + dumps(v, indent + 1) for k, v in value.items()) + "\n" + pad + "}"
    if isinstance(value, list):
        if not value:
            return "[]"
        if all(not isinstance(v, (dict, list)) for v in value):
            return "[" + ", ".join(json.dumps(v, ensure_ascii=False) for v in value) + "]"
        return "[\n" + ",\n".join(inner + dumps(v, indent + 1) for v in value) + "\n" + pad + "]"
    return json.dumps(value, ensure_ascii=False)


def main():
    export_dir, out_dir = sys.argv[1], sys.argv[2]
    v1 = V1(export_dir)
    os.makedirs(out_dir, exist_ok=True)
    for u in v1.universes:
        cfg = UNIVERSES[u["name"]]
        doc = build_universe(v1, u, cfg, {})
        path = os.path.join(out_dir, cfg["file"])
        with open(path, "w", encoding="utf-8") as f:
            f.write(dumps(doc) + "\n")
        n_matches = sum(len(r["matches"]) for s in doc["seasons"] for st in s["stages"] for g in st.get("groups", []) for r in g["rounds"]) + \
            sum(len(t["matches"]) for s in doc["seasons"] for st in s["stages"] for r in st.get("rounds", []) for t in r["ties"])
        print(f"{path}: {len(doc.get('nations', []))} nations, {len(doc['teams'])} teams, {len(doc.get('teamProfiles', []))} profiles, "
              f"{len(doc['seasons'])} seasons, {n_matches} matches, {os.path.getsize(path)} bytes")


if __name__ == "__main__":
    main()
