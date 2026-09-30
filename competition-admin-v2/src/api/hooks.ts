import { useMutation, useQuery, useQueryClient, type QueryKey } from '@tanstack/react-query'
import { get } from './client'
import type {
  AssetPage, AssetStatus, CompetitionDetail, CompetitionDto, ImportFileInfo, ImportRunDto, NationDto, PresetDto,
  SeasonSummary, SeasonView, SlotDto, StadiumDto, TableAsOf, TeamDetail, TeamDto, TeamType, UniverseDto,
} from './types'

export const keys = {
  universes: ['universes'] as const,
  universe: (key: string) => ['universe', key] as const,
  nations: (key: string) => ['nations', key] as const,
  stadiums: (key: string) => ['stadiums', key] as const,
  teams: (key: string, type?: TeamType) => ['teams', key, type ?? 'ALL'] as const,
  team: (id: number) => ['team', id] as const,
  nation: (id: number) => ['nation', id] as const,
  nationTeams: (id: number) => ['nation-teams', id] as const,
  competitions: (key: string) => ['competitions', key] as const,
  competition: (id: number) => ['competition', id] as const,
  seasons: (competitionId: number) => ['seasons', competitionId] as const,
  season: (id: number) => ['season', id] as const,
  table: (groupId: number, upTo?: number) => ['table', groupId, upTo ?? 'all'] as const,
  presets: ['presets'] as const,
  assets: (collection?: string, page = 0) => ['assets', collection ?? 'all', page] as const,
  slots: (ownerType: string, ownerId: number) => ['slots', ownerType, ownerId] as const,
  assetStatus: ['asset-status'] as const,
  importFiles: ['import-files'] as const,
  importRuns: ['import-runs'] as const,
}

export const useUniverses = () => useQuery({ queryKey: keys.universes, queryFn: () => get<UniverseDto[]>('/api/universes') })
export const useUniverse = (key: string) => useQuery({ queryKey: keys.universe(key), queryFn: () => get<UniverseDto>(`/api/universes/${key}`), enabled: !!key })
export const useNations = (key: string, enabled = true) => useQuery({ queryKey: keys.nations(key), queryFn: () => get<NationDto[]>(`/api/universes/${key}/nations`), enabled: !!key && enabled })
export const useStadiums = (key: string) => useQuery({ queryKey: keys.stadiums(key), queryFn: () => get<StadiumDto[]>(`/api/universes/${key}/stadiums`), enabled: !!key })
export const useTeams = (key: string, type?: TeamType) =>
  useQuery({ queryKey: keys.teams(key, type), queryFn: () => get<TeamDto[]>(`/api/universes/${key}/teams${type ? `?type=${type}` : ''}`), enabled: !!key })
export const useTeam = (id: number) => useQuery({ queryKey: keys.team(id), queryFn: () => get<TeamDetail>(`/api/teams/${id}`), enabled: Number.isFinite(id) })
export const useNation = (id: number) => useQuery({ queryKey: keys.nation(id), queryFn: () => get<NationDto>(`/api/nations/${id}`), enabled: Number.isFinite(id) })
export const useNationTeams = (id: number) => useQuery({ queryKey: keys.nationTeams(id), queryFn: () => get<TeamDto[]>(`/api/nations/${id}/teams`), enabled: Number.isFinite(id) })
export const useCompetitions = (key: string) => useQuery({ queryKey: keys.competitions(key), queryFn: () => get<CompetitionDto[]>(`/api/universes/${key}/competitions`), enabled: !!key })
export const useCompetition = (id: number) => useQuery({ queryKey: keys.competition(id), queryFn: () => get<CompetitionDetail>(`/api/competitions/${id}`), enabled: Number.isFinite(id) })
export const useSeasons = (competitionId: number) => useQuery({ queryKey: keys.seasons(competitionId), queryFn: () => get<SeasonSummary[]>(`/api/competitions/${competitionId}/seasons`), enabled: Number.isFinite(competitionId) })
export const useSeason = (id: number) => useQuery({ queryKey: keys.season(id), queryFn: () => get<SeasonView>(`/api/seasons/${id}`), enabled: Number.isFinite(id) })
export const useTableAsOf = (groupId: number, upTo?: number) =>
  useQuery({ queryKey: keys.table(groupId, upTo), queryFn: () => get<TableAsOf>(`/api/stage-groups/${groupId}/table${upTo ? `?upToRound=${upTo}` : ''}`), enabled: Number.isFinite(groupId) && upTo !== undefined })
export const usePresets = () => useQuery({ queryKey: keys.presets, queryFn: () => get<PresetDto[]>('/api/presets') })
export const useAssets = (collection?: string, page = 0) =>
  useQuery({ queryKey: keys.assets(collection, page), queryFn: () => get<AssetPage>(`/api/assets?page=${page}&size=60${collection ? `&collection=${encodeURIComponent(collection)}` : ''}`) })
export const useSlots = (ownerType: string, ownerId: number) =>
  useQuery({ queryKey: keys.slots(ownerType, ownerId), queryFn: () => get<SlotDto[]>(`/api/assets/slots?ownerType=${ownerType}&ownerId=${ownerId}`), enabled: Number.isFinite(ownerId) })
export const useAssetStatus = () => useQuery({ queryKey: keys.assetStatus, queryFn: () => get<AssetStatus>('/api/admin/assets/status') })
export const useImportFiles = () => useQuery({ queryKey: keys.importFiles, queryFn: () => get<ImportFileInfo[]>('/api/admin/imports/files') })
export const useImportRuns = () => useQuery({ queryKey: keys.importRuns, queryFn: () => get<ImportRunDto[]>('/api/admin/imports/runs'), refetchInterval: 15_000 })

/** Generic mutation that invalidates the given query keys on success. */
export function useAction<TArgs, TResult = unknown>(fn: (args: TArgs) => Promise<TResult>, invalidate: QueryKey[] | ((result: TResult, args: TArgs) => QueryKey[]) = []) {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: fn,
    onSuccess: (result, args) => {
      const list = typeof invalidate === 'function' ? invalidate(result, args) : invalidate
      list.forEach((k) => qc.invalidateQueries({ queryKey: k }))
    },
  })
}
