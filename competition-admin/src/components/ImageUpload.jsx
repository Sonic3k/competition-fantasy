import { useRef, useState } from 'react'
import api from '../api/client'

export default function ImageUpload({ endpoint, currentUrl, onUploaded, size = 48, shape = 'round' }) {
  const ref = useRef()
  const [uploading, setUploading] = useState(false)
  const [preview, setPreview] = useState(false)

  const upload = async (e) => {
    e.stopPropagation()
    const file = e.target.files[0]
    if (!file) return
    setUploading(true)
    try {
      const form = new FormData()
      form.append('file', file)
      const r = await api.post(endpoint, form, { headers: { 'Content-Type': 'multipart/form-data' } })
      onUploaded(r.data.url || r.data.cdnUrl)
    } catch (err) {
      alert('Upload failed: ' + (err.response?.data?.error || err.message))
    }
    setUploading(false)
    e.target.value = ''
  }

  const borderRadius = shape === 'round' ? '50%' : 6

  return (
    <>
      <input type="file" ref={ref} onChange={upload} accept="image/*" style={{ display: 'none' }} />

      {currentUrl ? (
        <div style={{ position: 'relative', flexShrink: 0 }}>
          <img src={currentUrl} alt="" onClick={() => setPreview(true)}
            style={{ width: size, height: size, borderRadius, objectFit: 'cover', cursor: 'pointer', border: '1px solid #eee' }} />
          <button onClick={(e) => { e.stopPropagation(); ref.current?.click() }}
            style={{ position: 'absolute', bottom: -4, right: -4, width: 18, height: 18, borderRadius: '50%',
              background: '#e94560', color: '#fff', border: '2px solid #fff', cursor: 'pointer',
              fontSize: 10, display: 'flex', alignItems: 'center', justifyContent: 'center', lineHeight: 1, padding: 0 }}>
            ✎
          </button>
        </div>
      ) : (
        <div onClick={() => ref.current?.click()} style={{
          width: size, height: size, borderRadius, flexShrink: 0, cursor: 'pointer',
          background: '#f5f5f5', display: 'flex', alignItems: 'center', justifyContent: 'center',
          border: '2px dashed #ddd',
        }}>
          {uploading ? <span style={{ color: '#999', fontSize: 10 }}>...</span> : <span style={{ color: '#bbb', fontSize: size * 0.35 }}>+</span>}
        </div>
      )}

      {/* Lightbox preview */}
      {preview && currentUrl && (
        <div onClick={() => setPreview(false)} style={{
          position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.8)', zIndex: 9999,
          display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer',
        }}>
          <img src={currentUrl} alt="" style={{ maxWidth: '90vw', maxHeight: '90vh', borderRadius: 8 }} />
        </div>
      )}
    </>
  )
}
