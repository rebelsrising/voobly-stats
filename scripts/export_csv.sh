#!/usr/bin/env bash
# Export all tables to csv.
docker exec voobly-postgres psql -U postgres -c "\copy match to '/voobly-postgres/match.csv' csv header"
docker exec voobly-postgres psql -U postgres -c "\copy player_data to '/voobly-postgres/player_data.csv' csv header"
docker exec voobly-postgres psql -U postgres -c "\copy match_job to '/voobly-postgres/match_job.csv' csv header"
docker exec voobly-postgres psql -U postgres -c "\copy player_job to '/voobly-postgres/player_job.csv' csv header"
