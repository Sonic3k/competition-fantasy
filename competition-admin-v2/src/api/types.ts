// DTOs mirroring competition-api-v2. JSON columns arrive parsed (raw JSON on the wire).

export type Json = Record<string, unknown> | unknown[] | string | number | boolean | null

export type UniverseType = 'FICTIONAL' | 'REAL'
export type TeamType = 'CLUB' | 'NATIONAL'
export type KitKind = 'HOME' | 'AWAY' | 'THIRD'
export type StageType = 'ROUND_ROBIN' | 'KNOCKOUT' | 'LEAGUE_PHASE' | 'SINGLE_MATCH'
export type SeasonStatus = 'PLANNED' | 'ONGOING' | 'COMPLETED'
export type MatchStatus = 'SCHEDULED' | 'PLAYED' | 'UNKNOWN' | 'CANCELLED'
export type MatchSource = 'NOTEBOOK' | 'MANUAL' | 'IMPORT' | 'SIMULATED'
export type Confidence = 'DRAFT' | 'VERIFIED'
export type HonourKind = 'CHAMPION' | 'RUNNER_UP' | 'THIRD' | 'FOURTH' | 'PROMOTED' | 'RELEGATED'
export type ZoneKind = 'CHAMPION' | 'QUALIFY' | 'PLAYOFF' | 'PROMOTION' | 'RELEGATION' | 'RELEGATION_PLAYOFF' | 'OTHER'

export interface Colors {
  primary?: string
  secondary?: string
  text?: string
  home?: { bg?: string; text?: string }
  away?: { bg?: string; text?: string }
}

export interface UniverseDto {
  id: number
  key: string
  name: string
  description: string | null
  type: UniverseType
  usesNations: boolean
  competitionCount: number
  teamCount: number
  nationCount: number
  avatarUrl: string | null
  bannerUrl: string | null
}

export interface NationDto {
  id: number
  universeId: number
  key: string
  name: string
  code: string
  colors: Colors | null
  description: string | null
  flagUrl: string | null
  emblemUrl: string | null
}

export interface StadiumDto {
  id: number
  universeId: number
  key: string
  name: string
  city: string | null
  capacity: number | null
  inspiredBy: string | null
  description: string | null
  imageUrl: string | null
}

export interface TeamDto {
  id: number
  universeId: number
  nationId: number | null
  nationCode: string | null
  nationName: string | null
  type: TeamType
  key: string
  name: string
  shortName: string | null
  code: string | null
  homeStadiumId: number | null
  description: string | null
  foundedYear: number | null
  dissolvedYear: number | null
  aliases: string[]
  logoUrl: string | null
}

export interface ProfileDto {
  id: number
  teamId: number
  year: number
  displayName: string | null
  sponsor: string | null
  colors: Colors | null
  notes: string | null
}

export interface KitDto {
  id: number
  teamId: number
  year: number
  kind: KitKind
  shirtColor: string | null
  shortsColor: string | null
  socksColor: string | null
  sponsor: string | null
  imageAssetId: number | null
  imageUrl: string | null
}

export interface SeasonPlayed {
  seasonId: number
  seasonName: string
  year: number | null
  competitionId: number
  competitionName: string | null
  status: SeasonStatus
  honours: HonourKind[]
}

export interface TeamDetail {
  team: TeamDto
  profiles: ProfileDto[]
  kits: KitDto[]
  seasons: SeasonPlayed[]
}

export interface CompetitionDto {
  id: number
  universeId: number
  universeKey: string | null
  universeName: string | null
  key: string
  name: string
  sport: string
  teamLevel: TeamType
  tier: number | null
  description: string | null
  seasonCount: number
  logoUrl: string | null
}

export interface SeasonSummary {
  id: number
  competitionId: number
  key: string
  name: string
  year: number | null
  startDate: string | null
  endDate: string | null
  status: SeasonStatus
  presetKey: string | null
  championTeamId: number | null
  championName: string | null
  teamCount: number
}

export interface CompetitionDetail {
  competition: CompetitionDto
  seasons: SeasonSummary[]
}

// ---- format definition ----

export interface Zone { positions: number[]; kind: ZoneKind; label?: string }
export interface KoRoundDef { key: string; name?: string; legs?: number; placement?: boolean }
export interface StageDef {
  key: string
  name?: string
  type: StageType
  groups?: number
  teamsPerGroup?: number
  meetings?: number
  venue?: string
  matchesPerTeam?: number
  pots?: number
  tiebreakers?: string[]
  zones?: Zone[]
  rounds?: KoRoundDef[]
  legs?: number
  decider?: string[]
  replays?: number
  thirdPlace?: boolean
  entry?: Record<string, unknown>
  carryOver?: Record<string, unknown>
}
export interface FormatDefinition {
  key: string
  name?: string
  family?: string
  teams?: number
  points?: { win?: number; draw?: number; loss?: number }
  stages: StageDef[]
}

export interface PresetDto {
  id: number
  key: string
  name: string
  family: string | null
  builtin: boolean
  definition: FormatDefinition
}

// ---- season view ----

export interface TeamRef {
  id: number
  key: string
  name: string
  shortName: string | null
  code: string | null
  type: TeamType
  nationId: number | null
  nationCode: string | null
  nationName: string | null
  colors: Colors | null
  nationColors: Colors | null
  displayName: string | null
  sponsor: string | null
  logoUrl: string | null
  flagUrl: string | null
}

export interface MatchView {
  id: number
  roundId: number | null
  tieId: number | null
  leg: number
  homeTeamId: number | null
  awayTeamId: number | null
  date: string | null
  stadiumId: number | null
  status: MatchStatus
  homeScore: number | null
  awayScore: number | null
  homeEt: number | null
  awayEt: number | null
  homePens: number | null
  awayPens: number | null
  walkover: 'HOME' | 'AWAY' | null
  winnerTeamId: number | null
  source: MatchSource
  sourceRef: string | null
  confidence: Confidence
  notes: string | null
}

export interface StandingRowView {
  teamId: number
  position: number
  played: number
  won: number
  drawn: number
  lost: number
  goalsFor: number
  goalsAgainst: number
  goalDiff: number
  points: number
  adjustment: number
  zone: ZoneKind | null
  tieNote: string | null
}

export interface StandingView { id: number; type: 'CALCULATED' | 'RECORDED'; checkpointRound: number; rows: StandingRowView[] }

export interface RoundView { id: number; number: number; name: string; startDate: string | null; endDate: string | null; matches: MatchView[] }

export interface GroupTeamView { teamId: number | null; position: number; entrySource: Record<string, unknown> | null }

export interface GroupView {
  id: number
  key: string
  name: string
  ordinal: number
  teams: GroupTeamView[]
  rounds: RoundView[]
  calculated: StandingView | null
  recorded: StandingView[]
}

export interface TieView {
  id: number
  position: number
  homeSource: Record<string, unknown> | null
  awaySource: Record<string, unknown> | null
  homeTeamId: number | null
  awayTeamId: number | null
  winnerTeamId: number | null
  resolution: string | null
  matches: MatchView[]
}

export interface KoRoundView { id: number; key: string; name: string; ordinal: number; legs: number; placement: boolean; ties: TieView[] }

export interface StageView {
  id: number
  key: string
  name: string
  type: StageType
  ordinal: number
  config: StageDef | null
  groups: GroupView[]
  koRounds: KoRoundView[]
}

export interface HonourView { id: number; teamId: number; kind: HonourKind; manual: boolean }

export interface SeasonView {
  season: { id: number; key: string; name: string; year: number | null; startDate: string | null; endDate: string | null; status: SeasonStatus; presetKey: string | null; notes: string | null }
  competition: { id: number; key: string; name: string; sport: string; teamLevel: TeamType; tier: number | null }
  universe: { id: number; key: string; name: string; type: UniverseType; usesNations: boolean }
  format: FormatDefinition | null
  teams: Record<string, TeamRef>
  seasonTeams: Array<{ teamId: number; seed: number | null; pot: number | null }>
  stages: StageView[]
  honours: HonourView[]
}

export interface RankedRow {
  teamId: number
  position: number
  played: number
  won: number
  drawn: number
  lost: number
  goalsFor: number
  goalsAgainst: number
  goalDiff: number
  points: number
  adjustment: number
  tieNote: string | null
}

export interface TableAsOf { stageGroupId: number; upToRound: number | null; rows: RankedRow[]; teams: Record<string, TeamRef> }

// ---- assets ----

export interface AssetDto {
  id: number
  storageKey: string
  url: string
  contentType: string
  sizeBytes: number | null
  width: number | null
  height: number | null
  title: string | null
  collection: string | null
  tags: string[]
}
export interface AssetPage { items: AssetDto[]; total: number; page: number; size: number }
export interface SlotDto { id: number; ownerType: string; ownerId: number; slot: string; assetId: number; url: string | null; current: boolean }
export interface AssetStatus { configured: boolean; prefix: string; ownerTypes: string[]; slots: string[] }

// ---- script manager ----

export interface ImportFileInfo { path: string; sizeBytes: number; modifiedAt: string | null; example: boolean }
export interface ImportReport {
  runId: number
  status: 'SUCCESS' | 'FAILED'
  dryRun: boolean
  counts: Record<string, number>
  warnings: string[]
  errors: string[]
  log: string[]
}
export interface ImportRunDto {
  id: number
  fileName: string
  source: 'REPO' | 'UPLOAD'
  checksum: string | null
  dryRun: boolean
  status: 'RUNNING' | 'SUCCESS' | 'FAILED' | 'REVERTED'
  startedAt: string
  finishedAt: string | null
  summary: { counts?: Record<string, number>; warnings?: number; errors?: string[] } | null
  log: string | null
  changes: number
}

export interface LoginResponse { token: string; username: string; expiresInHours: number }
