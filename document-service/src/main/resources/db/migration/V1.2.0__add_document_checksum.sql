ALTER TABLE public.documents ADD COLUMN checksum varchar(200);
ALTER TABLE public.documents ADD CONSTRAINT customer_id_checksum_key UNIQUE (customer_id, checksum);
