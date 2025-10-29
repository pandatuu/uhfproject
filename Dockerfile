# Multi-stage Docker build for Android APK
FROM openjdk:11-jdk AS base

# Install required packages
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    curl \
    git \
    && rm -rf /var/lib/apt/lists/*

# Set up Android SDK
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV ANDROID_HOME=${ANDROID_SDK_ROOT}
ENV PATH="${ANDROID_HOME}/tools:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools:${PATH}"

# Download and install Android SDK command line tools
RUN wget -q https://dl.google.com/android/repository/commandlinetools-linux-7583922_latest.zip -O /tmp/commandlinetools.zip && \
    mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools && \
    unzip -q /tmp/commandlinetools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools && \
    mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest && \
    rm /tmp/commandlinetools.zip

# Install Android SDK components
RUN yes | ${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager --licenses
RUN ${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager \
    "platform-tools" \
    "platforms;android-31" \
    "build-tools;31.0.0" \
    "extras;android;m2repository" \
    "extras;google;m2repository" \
    "cmake;3.18.1" \
    "ndk;21.4.7075529"

# Set up Gradle
ENV GRADLE_HOME=/opt/gradle
ENV PATH="${GRADLE_HOME}/bin:${PATH}"
RUN wget -q https://services.gradle.org/distributions/gradle-7.4-bin.zip -O /tmp/gradle.zip && \
    mkdir -p ${GRADLE_HOME} && \
    unzip -q /tmp/gradle.zip -d /opt && \
    mv /opt/gradle-7.4 ${GRADLE_HOME} && \
    rm /tmp/gradle.zip

# Create app directory
WORKDIR /app

# Copy project files
COPY . .

# Create keystore directory in app module (where build.gradle expects it)
RUN mkdir -p app/keystore

# Create debug keystore with known credentials (delete if exists first)
RUN rm -f app/keystore/debug.keystore && keytool -genkeypair \
    -dname "CN=Android Debug, OU=Debug, O=Debug, L=Debug, ST=Debug, C=US" \
    -alias androiddebugkey \
    -keypass android \
    -keystore app/keystore/debug.keystore \
    -storepass android \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -noprompt

# Set up default keystore path
ENV KEYSTORE_PATH=app/keystore/ZHunis.jks

# Fix Windows line endings and make gradlew executable
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Build stage
FROM base AS builder

# Define ARGs to receive secrets from the 'docker build' command
ARG KEYSTORE_ALIAS
ARG KEY_PASSWORD
ARG STORE_PASSWORD

# Execute gradlew, passing ARGs as temporary environment variables
# This allows the Gradle script to read them via System.getenv()
RUN KEYSTORE_ALIAS=$KEYSTORE_ALIAS \
    KEY_PASSWORD=$KEY_PASSWORD \
    STORE_PASSWORD=$STORE_PASSWORD \
    ./gradlew assembleRelease --no-daemon --parallel

# Final stage - just the APK
FROM alpine:latest AS final

# Declare the argument for the output APK name
ARG APK_FILENAME=singPostSgDevV1.apk 

RUN apk --no-cache add ca-certificates
WORKDIR /app

# Use COPY --from and the build argument
COPY --from=builder /app/app/build/outputs/apk/release/app-release.apk ./$APK_FILENAME

# Command to run (optional, for verification)
CMD ["sh", "-c", "echo 'APK ready: ${APK_FILENAME}'; ls -la ${APK_FILENAME}"]