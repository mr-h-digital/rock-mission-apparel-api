-- One-time data repair for malformed product image URLs.
-- Safe to run multiple times: only updates rows that need normalization.
UPDATE products p
SET image_url = fixed.fixed_url
FROM (
    SELECT id,
           CASE
               WHEN cleaned_url ~* '^https?://'
                   THEN cleaned_url
               WHEN cleaned_url ~* '^https:/[^/]'
                   THEN regexp_replace(cleaned_url, '^https:/([^/])', 'https://\1', 'i')
               WHEN cleaned_url ~* '^http:/[^/]'
                   THEN regexp_replace(cleaned_url, '^http:/([^/])', 'http://\1', 'i')
               WHEN cleaned_url LIKE '//%'
                   THEN 'https:' || cleaned_url
               WHEN cleaned_url ~* '^[a-z0-9.-]+\.[a-z]{2,}(/|$)'
                   THEN 'https://' || cleaned_url
               ELSE cleaned_url
           END AS fixed_url
    FROM (
        SELECT id, btrim(image_url) AS cleaned_url
        FROM products
        WHERE image_url IS NOT NULL
          AND btrim(image_url) <> ''
    ) source
) fixed
WHERE p.id = fixed.id
  AND p.image_url IS DISTINCT FROM fixed.fixed_url;
