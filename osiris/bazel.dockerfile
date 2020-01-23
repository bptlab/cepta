FROM l.gcr.io/google/bazel:latest AS BUILD

ARG LOCATION=osiris/query
ARG TARGET=query

WORKDIR /app
COPY . /app/
# RUN bazel build //${LOCATION}:${TARGET}

# RUN cp bazel-bin/${LOCATION}/linux_amd64_stripped/${TARGET} /app.run

# FROM golang:latest
# ENV ENTRY=/app/bazel-bin/osiris/query/linux_amd64_stripped/query
# WORKDIR /app
# COPY --from=BUILD /app/bazel-bin/ /app/bazel-bin/
# COPY --from=BUILD /app.run /app.run
# RUN ls -lia /app/bazel-bin/
# ENTRYPOINT /app.run
ENTRYPOINT bazel run //${LOCATION}:${TARGET} --