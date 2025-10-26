FROM redis:7-alpine

VOLUME ["/data"]

ENV REDIS_PASSWORD=""

CMD ["/bin/sh", "-c", "exec redis-server --appendonly yes ${REDIS_PASSWORD:+--requirepass $REDIS_PASSWORD}"]
