-- Collections (hierarchical)
CREATE TABLE collections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(30) NOT NULL DEFAULT 'GENERAL',
    parent_id UUID REFERENCES collections(id) ON DELETE SET NULL,
    universe_id UUID REFERENCES universes(id) ON DELETE SET NULL,
    cover_media_id UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Media files
CREATE TABLE media_files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    filename VARCHAR(500) NOT NULL,
    cdn_url VARCHAR(1000) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    width INTEGER,
    height INTEGER,
    collection_id UUID REFERENCES collections(id) ON DELETE SET NULL,
    source_type VARCHAR(20) NOT NULL DEFAULT 'UPLOAD',
    prompt_used TEXT,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Tags
CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    color VARCHAR(7),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Media-Tag junction
CREATE TABLE media_file_tags (
    media_file_id UUID NOT NULL REFERENCES media_files(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (media_file_id, tag_id)
);

-- Cover FK (deferred because media_files didn't exist yet)
ALTER TABLE collections ADD CONSTRAINT fk_collection_cover FOREIGN KEY (cover_media_id) REFERENCES media_files(id) ON DELETE SET NULL;

-- Entity links to media
ALTER TABLE teams ADD COLUMN logo_media_id UUID REFERENCES media_files(id) ON DELETE SET NULL;
ALTER TABLE nations ADD COLUMN flag_media_id UUID REFERENCES media_files(id) ON DELETE SET NULL;
ALTER TABLE universes ADD COLUMN avatar_media_id UUID REFERENCES media_files(id) ON DELETE SET NULL;

-- Indexes
CREATE INDEX idx_media_files_collection ON media_files(collection_id);
CREATE INDEX idx_media_files_source ON media_files(source_type);
CREATE INDEX idx_collections_parent ON collections(parent_id);
CREATE INDEX idx_collections_universe ON collections(universe_id);
