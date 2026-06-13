-- =====================================================================
-- V20: Backfill tournament format presets onto existing seasons.
--   * Season containing any KNOCKOUT stage  -> group+knockout cup preset
--   * All remaining seasons (Othelose)       -> double round-robin league
-- This only TAGS the format; it does not touch matches/standings, so all
-- existing data renders exactly as before. Knockout winner/tie backfill is
-- intentionally deferred to the advance/recompute step (handled correctly
-- for both single-leg and two-leg ties there).
-- =====================================================================

UPDATE seasons s
   SET format_preset_key = 'CUP_C1_WONDER_EMPIRES'
 WHERE format_preset_key IS NULL
   AND EXISTS (SELECT 1 FROM stages st
                WHERE st.season_id = s.id
                  AND st.type = 'KNOCKOUT');

UPDATE seasons
   SET format_preset_key = 'LEAGUE_DOUBLE_RR'
 WHERE format_preset_key IS NULL;
