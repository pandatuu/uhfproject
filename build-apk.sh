#!/bin/bash
set -e

echo "🏗️ Building UHF Project APK..."

# --- 🎯 User-Changeable Variable (for filename) ---
APK_NAME="SingpostProject_$(date +%Y%m%d).apk" 
# --------------------------------------------------

# --- 🔑 Keystore Credentials (SET YOUR PRESET VALUES HERE!) ---
# These are the credentials for your app/keystore/Zhunis.jks file.
JKS_ALIAS="ZHunis"  # <-- Set your Keystore Alias
JKS_KEY_PASS="123456"            # <-- Set your Key Password
JKS_STORE_PASS="123456"        # <-- Set your Store Password
# ------------------------------------------------------------

# Note: Debug keystore is automatically created in the Dockerfile

# ===================================================================
# ===                     NEW & MODIFIED CODE                    ===
# ===================================================================

# 1. Define the log filename based on the APK name
LOG_NAME="${APK_NAME%.apk}.log"

# 2. Create the output directory BEFORE we need to write to it
mkdir -p output

echo "📋 Build logs will be saved to: output/${LOG_NAME}"
echo "--------------------------------------------------"

# 3. Run the build, piping output to 'tee' to display AND save it
#    - `2>&1` redirects stderr (error logs) to stdout (standard logs)
#    - `|` (pipe) sends the combined output to the next command
#    - `tee "output/${LOG_NAME}"` displays the logs in the terminal AND saves them to the file
#    - add     --progress=plain \ for full debug in console
docker build \
    --build-arg APK_FILENAME="${APK_NAME}" \
    --build-arg KEYSTORE_ALIAS="${JKS_ALIAS}" \
    --build-arg KEY_PASSWORD="${JKS_KEY_PASS}" \
    --build-arg STORE_PASSWORD="${JKS_STORE_PASS}" \
    -t uhfproject-final . 2>&1 | tee "output/${LOG_NAME}"

# ===================================================================
# ===                       END OF CHANGES                        ===
# ===================================================================


# Create a temporary container to extract the APK
docker create --name uhf_extractor uhfproject-final
docker cp uhf_extractor:/app/${APK_NAME} output/${APK_NAME}
docker rm uhf_extractor

if [ -f "output/${APK_NAME}" ]; then
    echo "--------------------------------------------------"
    echo "✅ APK built successfully: output/${APK_NAME}"
    echo "📋 Full build logs saved to: output/${LOG_NAME}"
else
    echo "--------------------------------------------------"
    echo "❌ Build failed - ${APK_NAME} not found in output directory."
    echo "📋 Check the logs for details: output/${LOG_NAME}"
    exit 1
fi