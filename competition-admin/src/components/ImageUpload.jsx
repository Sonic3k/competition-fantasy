import { useRef, useState } from 'react'
import api from '../api/client'

export default function ImageUpload({ endpoint, currentUrl, onUploaded, size = 48, shape = 'round' }) {
  const ref = useRef()
  const [uploading, setUploading] = useState(false)

  const upload = async (e) => {
    const file = e.target.files[0]
    if (!file) return
    setUploading(true)
    try {
      const form = new FormData()
      form.append('file', file)
      const r = await api.post(endpoint, form, { headers: { 'Content-Type': 'multipart/form-data' } })
      onUploaded(r.data.url)
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
      <div onClick={() => ref.current?.click()} style={{
        width: size, height: size, borderRadius, flexShrink: 0, cursor: 'pointer',
        background: currentUrl ? `url(${currentUrl}) center/cover no-repeat` : '#f0f0f0',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        border: '2px dashed #ddd', overflow: 'hidden', position: 'relative',
      }}>
        {!currentUrl && !uploading && <span style={{ color: '#bbb', fontSize: size * 0.35 }}>+</span>}
        {uploading && <span style={{ color: '#999', fontSize: 10 }}>...</span>}
      </div>
    </>
  )
}
