```sql
SET NOCOUNT ON;

DECLARE @i INT = 1;
DECLARE @total INT = 10000000;
DECLARE @batchSize INT = 500;

WHILE @i <= @total
BEGIN
    BEGIN TRANSACTION;

    DECLARE @j INT = 1;
    WHILE @j <= @batchSize AND @i <= @total
    BEGIN
        INSERT INTO outbox_event (
            aggregate_id,
            aggregate_type,
            event_type,
            payload,
            status,
            created_at,
            processed_at,
            retry_count
        )
        VALUES (
            CONCAT('agg-', CAST(ABS(CHECKSUM(NEWID())) AS NVARCHAR(255))),
            'Order',
            0,
            CONCAT('{"data": "Payload ', @i, '"}'),
            0,
            getdate(),
            NULL,
            ABS(CHECKSUM(NEWID())) % 5
        );

        SET @i += 1;
        SET @j += 1;
    END

    COMMIT TRANSACTION;
END;

PRINT 'Inserção em massa com commit a cada 100 registros concluída.';

```


```sql

SELECT TOP 100
    -- CPU médio por execução (µs → ms)
    (qs.total_worker_time * 1.0 / qs.execution_count) / 1000.0 
      AS Avg_CPU_ms,
    -- CPU total (µs → ms)
    qs.total_worker_time * 1.0 / 1000.0 
      AS Total_CPU_ms,
    qs.execution_count AS ExecutionCount,
    -- Tempo decorrido médio (µs → ms)
    (qs.total_elapsed_time * 1.0 / qs.execution_count) / 1000.0 
      AS Avg_Elapsed_ms,
    -- Texto da query
    SUBSTRING(
      st.text,
      (qs.statement_start_offset/2) + 1,
      (
        (CASE qs.statement_end_offset
           WHEN -1 THEN DATALENGTH(st.text)
           ELSE qs.statement_end_offset
         END
         - qs.statement_start_offset
        )/2
      ) + 1
    ) AS QueryText,
    qp.query_plan,
    st.text
FROM sys.dm_exec_query_stats AS qs
CROSS APPLY sys.dm_exec_sql_text(qs.sql_handle)    AS st
CROSS APPLY sys.dm_exec_query_plan(qs.plan_handle) AS qp
WHERE st.text LIKE '%outbox%'
ORDER BY qs.execution_count DESC;
```

```sql
SELECT 
    OBJECT_NAME(ps.object_id)       AS TableName,
    i.name                          AS IndexName,
    ps.index_type_desc,
    ps.avg_fragmentation_in_percent,
    ps.page_count
FROM sys.dm_db_index_physical_stats(
       DB_ID(),      -- current database
       NULL,         -- all tables
       NULL,         -- all indexes
       NULL,         -- all partitions
       'LIMITED'     -- sampling mode; para fullscan use 'DETAILED'
     ) AS ps
JOIN sys.indexes AS i
  ON ps.object_id = i.object_id 
 AND ps.index_id  = i.index_id
WHERE ps.page_count > 1000      -- só faz sentido em índices grandes
  AND ps.avg_fragmentation_in_percent > 5
ORDER BY ps.avg_fragmentation_in_percent DESC;
```

```sql
DBCC SHRINKDATABASE (master, 1);
EXEC sp_updatestats;

ALTER INDEX ALL
  ON dbo.Outbox
  REBUILD
  WITH (FILLFACTOR = 85);

UPDATE STATISTICS dbo.outbox WITH FULLSCAN;
UPDATE STATISTICS dbo.outbox IX_Outbox_Event_Type_Status_Covering;

```

```sql

SELECT 
    OBJECT_NAME(s.object_id) AS Tabela,
    i.name AS Indice,
    s.user_seeks,
    s.user_scans,
    s.user_lookups,
    s.user_updates,
    i.type_desc AS TipoIndice
FROM sys.dm_db_index_usage_stats AS s
INNER JOIN sys.indexes AS i 
    ON i.object_id = s.object_id AND i.index_id = s.index_id
WHERE OBJECTPROPERTY(s.object_id, 'outbox') = 1
ORDER BY (s.user_seeks + s.user_scans + s.user_lookups) DESC;

```

```sql

SELECT
    c.session_id,
    s.login_name,
    s.host_name,
    s.program_name,
    c.client_net_address,
    c.connect_time
FROM sys.dm_exec_connections c
JOIN sys.dm_exec_sessions s ON c.session_id = s.session_id;
```

```sql
SELECT program_name, host_name, login_name 
FROM sys.dm_exec_sessions
```

```sql

SELECT event_type, COUNT(*) AS total_pendentes
FROM outbox_event WITH (NOLOCK)
WHERE status = 0
GROUP BY event_type
ORDER BY event_type asc;


delete from outbox 

```