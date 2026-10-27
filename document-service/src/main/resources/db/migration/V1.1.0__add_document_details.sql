ALTER TABLE public.documents ADD COLUMN uploaded_at timestamp;
ALTER TABLE public.documents ADD COLUMN uploaded_by varchar(30);
ALTER TABLE public.documents ADD COLUMN file_size integer;