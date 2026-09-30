import { useRef, useState } from 'react'
import { get, post, upload } from '@/api/client'
import { keys, useAction, useImportFiles, useImportRuns } from '@/api/hooks'
import type { ImportReport, ImportRunDto } from '@/api/types'
import { Badge, Button, Confirm, ErrorBox, PageTitle, Spinner, cx } from '@/components/ui'
import { fmtInstant } from '@/lib/format'

const ALL_KEYS = [keys.importRuns, keys.universes, ['universe'], ['nations'], ['stadiums'], ['teams'], ['team'], ['competitions'], ['competition'], ['seasons'], ['season']]

/** Run committed JSON import files (or an upload) against the database, dry-run first. */
export function ScriptManager() {
  const files = useImportFiles()
  const runs = useImportRuns()
  const [report, setReport] = useState<ImportReport | null>(null)
  const [preview, setPreview] = useState<{ path: string; text: string } | null>(null)
  const [confirmRun, setConfirmRun] = useState<string | null>(null)
  const [revertId, setRevertId] = useState<number | null>(null)
  const [openRun, setOpenRun] = useState<number | null>(null)
  const fileRef = useRef<HTMLInputElement>(null)
  const [dryUpload, setDryUpload] = useState(true)

  const run = useAction((a: { file: string; dryRun: boolean }) => post<ImportReport>('/api/admin/imports/run', a), ALL_KEYS)
  const runUpload = useAction((a: { file: File; dryRun: boolean }) => { const f = new FormData(); f.append('file', a.file); return upload<ImportReport>(`/api/admin/imports/upload?dryRun=${a.dryRun}`, f) }, ALL_KEYS)
  const revert = useAction((id: number) => post<ImportRunDto>(`/api/admin/imports/runs/${id}/revert`), ALL_KEYS)

  const onReport = (r: ImportReport) => { setReport(r); setOpenRun(null) }

  return (
    <div>
      <PageTitle title="Script manager">
        Import files live in <code className="rounded-sm bg-rule-soft px-1">competition-api-v2/imports/</code>. A dry run executes the whole file and rolls back, so its report is exact.
      </PageTitle>
      <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_minmax(0,1fr)]">
        <section>
          <div className="mb-2 flex items-center justify-between">
            <h2 className="font-condensed text-lg font-semibold">Files in the repository</h2>
            <Button size="sm" variant="ghost" onClick={() => files.refetch()}>Refresh</Button>
          </div>
          {files.isLoading && <Spinner />}
          <ErrorBox error={files.error} />
          <ul className="sheet divide-y divide-rule-soft text-sm">
            {files.data?.map((f) => (
              <li key={f.path} className="flex items-center gap-2 px-3 py-2">
                <button type="button" className="min-w-0 flex-1 truncate text-left font-mono text-[13px] hover:underline" onClick={async () => setPreview({ path: f.path, text: await get<string>(`/api/admin/imports/files/content?path=${encodeURIComponent(f.path)}`).then((t) => (typeof t === 'string' ? t : JSON.stringify(t, null, 2))) })}>{f.path}</button>
                {f.example && <Badge>example</Badge>}
                <span className="tnum shrink-0 text-xs text-ink-3">{(f.sizeBytes / 1024).toFixed(1)} kB</span>
                <Button size="sm" onClick={() => run.mutate({ file: f.path, dryRun: true }, { onSuccess: onReport })} disabled={run.isPending}>Dry run</Button>
                <Button size="sm" variant="primary" onClick={() => setConfirmRun(f.path)} disabled={run.isPending}>Run</Button>
              </li>
            ))}
            {files.data?.length === 0 && <li className="px-3 py-6 text-center text-ink-3">No JSON files found in the import folder.</li>}
          </ul>

          <div className="mt-6 sheet p-3">
            <h3 className="font-condensed text-base font-semibold">Upload a file instead</h3>
            <div className="mt-2 flex flex-wrap items-center gap-3 text-sm">
              <input ref={fileRef} type="file" accept="application/json,.json" className="text-sm" />
              <label className="flex items-center gap-1.5"><input type="checkbox" checked={dryUpload} onChange={(e) => setDryUpload(e.target.checked)} /> dry run</label>
              <Button size="sm" variant="primary" disabled={runUpload.isPending} onClick={() => { const f = fileRef.current?.files?.[0]; if (f) runUpload.mutate({ file: f, dryRun: dryUpload }, { onSuccess: onReport }) }}>Upload and run</Button>
            </div>
            <ErrorBox error={runUpload.error} className="mt-2" />
          </div>
          <ErrorBox error={run.error} className="mt-3" />
          {(run.isPending || runUpload.isPending) && <Spinner label="Running import…" />}
        </section>

        <section>
          {report && <Report report={report} onClose={() => setReport(null)} />}
          <div className="mb-2 flex items-center justify-between">
            <h2 className="font-condensed text-lg font-semibold">Run history</h2>
          </div>
          {runs.isLoading && <Spinner />}
          <ul className="sheet divide-y divide-rule-soft text-sm">
            {runs.data?.map((r) => (
              <li key={r.id}>
                <button type="button" className="flex w-full flex-wrap items-center gap-2 px-3 py-2 text-left hover:bg-paper" onClick={() => setOpenRun(openRun === r.id ? null : r.id)}>
                  <RunStatus r={r} />
                  <span className="font-mono text-[13px]">{r.fileName}</span>
                  {r.dryRun && <Badge>dry</Badge>}
                  {r.source === 'UPLOAD' && <Badge>upload</Badge>}
                  <span className="ml-auto text-xs text-ink-3">{fmtInstant(r.startedAt)}</span>
                </button>
                {openRun === r.id && (
                  <div className="border-t border-rule-soft bg-paper/60 px-3 py-2 text-xs">
                    {r.summary?.counts && <p className="mb-1 text-ink-2">{Object.entries(r.summary.counts).map(([k, v]) => `${k}: ${v}`).join(' · ')}</p>}
                    <pre className="max-h-64 overflow-auto whitespace-pre-wrap text-ink-2">{r.log}</pre>
                    {r.status === 'SUCCESS' && !r.dryRun && r.changes > 0 && <div className="mt-2"><Button size="sm" variant="danger" onClick={() => setRevertId(r.id)} disabled={revert.isPending}>Revert ({r.changes} records)</Button></div>}
                  </div>
                )}
              </li>
            ))}
            {runs.data?.length === 0 && <li className="px-3 py-6 text-center text-ink-3">Nothing has been run yet.</li>}
          </ul>
          <ErrorBox error={revert.error} className="mt-3" />
        </section>
      </div>

      {preview && (
        <div className="mt-6">
          <div className="mb-1 flex items-center justify-between"><h3 className="font-mono text-sm">{preview.path}</h3><Button size="sm" variant="ghost" onClick={() => setPreview(null)}>Close</Button></div>
          <pre className="sheet max-h-[60vh] overflow-auto p-3 text-xs">{preview.text}</pre>
        </div>
      )}

      <Confirm open={!!confirmRun} onClose={() => setConfirmRun(null)} title="Run import" message={`Run ${confirmRun} for real? Seasons in the file replace existing ones with the same key.`} busy={run.isPending} onConfirm={() => confirmRun && run.mutate({ file: confirmRun, dryRun: false }, { onSuccess: (r) => { onReport(r); setConfirmRun(null) } })} />
      <Confirm open={revertId != null} onClose={() => setRevertId(null)} title="Revert run" message="Delete every record this run created? Updated or replaced records are not restored." busy={revert.isPending} onConfirm={() => revertId != null && revert.mutate(revertId, { onSuccess: () => setRevertId(null) })} />
    </div>
  )
}

function RunStatus({ r }: { r: ImportRunDto }) {
  const tone = r.status === 'SUCCESS' ? 'green' : r.status === 'FAILED' ? 'red' : r.status === 'REVERTED' ? 'amber' : 'blue'
  return <Badge tone={tone}>{r.status.toLowerCase()}</Badge>
}

function Report({ report, onClose }: { report: ImportReport; onClose: () => void }) {
  const ok = report.status === 'SUCCESS'
  return (
    <div className={cx('mb-6 rounded-sm border p-4 text-sm', ok ? 'border-qualify/40 bg-qualify-soft' : 'border-relegation/40 bg-relegation-soft')}>
      <div className="flex items-center justify-between">
        <h3 className="font-condensed text-lg font-semibold">{report.dryRun ? 'Dry run' : 'Import'} {ok ? 'succeeded' : 'failed'}{report.dryRun && ok ? ' — nothing was written' : ''}</h3>
        <Button size="sm" variant="ghost" onClick={onClose}>Close</Button>
      </div>
      {Object.keys(report.counts).length > 0 && (
        <ul className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-ink-2">
          {Object.entries(report.counts).map(([k, v]) => <li key={k}><b className="tnum">{v}</b> {k.toLowerCase()}</li>)}
        </ul>
      )}
      {report.errors.length > 0 && (
        <div className="mt-3">
          <div className="font-medium text-relegation">Errors ({report.errors.length})</div>
          <ul className="mt-1 list-disc pl-5 text-relegation">{report.errors.map((e, i) => <li key={i}>{e}</li>)}</ul>
        </div>
      )}
      {report.warnings.length > 0 && (
        <details className="mt-3">
          <summary className="cursor-pointer font-medium text-playoff">Warnings ({report.warnings.length})</summary>
          <ul className="mt-1 list-disc pl-5 text-ink-2">{report.warnings.map((w, i) => <li key={i}>{w}</li>)}</ul>
        </details>
      )}
      {report.log.length > 0 && (
        <details className="mt-3">
          <summary className="cursor-pointer text-ink-2">Log</summary>
          <pre className="mt-1 whitespace-pre-wrap text-xs text-ink-2">{report.log.join('\n')}</pre>
        </details>
      )}
    </div>
  )
}
