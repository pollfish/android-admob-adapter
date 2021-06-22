no_publish=false
keep_open=false
publish_only=false

while test $# -gt 0; do
    case "$1" in
        -h|--help)
            echo "release - Release Pollfish AdMob Android Adapter"
            echo " "
            echo "release [options]"
            echo " "
            echo "options:"
            echo "-h, --help                show brief help"
            echo "--publish-only            execute only publishing tasks"
            echo "--no-publish              skip publishing tasks"
            echo "--keep-open               keep sonatype repository open and don't release"
            echo "--no-upload               Do not upload to Google Play"
            exit 0
            ;;
        --no-publish*)
            shift
            no_publish=true
            keep_open=true
            no_upload=true
            shift
            ;;
        --keep-open*)
            keep_open=true
            shift
            ;;
        --no-upload*)
            no_upload=true
            shift
            ;;
        --publish-only*)
            publish_only=true
            shift
            ;;
    esac
done

if [ "$publish_only" = false ] ; then

    # Clean build folder

    if ./gradlew clean; then
        echo "Clean task succeeded"
    else
        echo "Clean task failed"
        exit 1
    fi

    # Build Pollfish AdMob Adapter

    if ./gradlew :pollfish-admob:build; then
        echo "Build task succeeded"
    else
        echo "Build task failed. Retrying"

        if ./gradlew :pollfish-admob:build; then
            echo "Build task succeeded"
        else
            echo "Build task failed."
        fi
    fi

    # Package public distribution .zip files

    if ./gradlew :pollfish:packageDistributions; then
        echo "Packing Distributions succeeded"
    else
        echo "Packing Distributions failed"
        exit 1
    fi

else
    echo "Skipping Building"
fi

if [ "$no_publish" = false ] ; then

    # Upload Pollfish AdMob Adapter aar to Sonatype repository

    if ./gradlew :pollfish:publishAllPublicationsToSonatypeRepository; then
        echo "Upload to Sonatype tak succeeded"
    else 
        echo "Upload to Sonatype task failed"
        exit 1
    fi

    for entry in pollfish-admob/build/dist/public/*
    do
        file_name="${entry##*/}"
        slashed_entry="${entry// /\\ }"

        # Upload public dist .zip files to Google Cloud Bucket

        eval "gsutil cp ${slashed_entry} gs://pollfish_production/sdk/AdMob/"
        if [ $? -eq 0 ]; then
            echo "Upload ${file_name} succeeded"
        else
            echo "Upload ${file_name} failed"
            exit 1
        fi

        slashed_file_name="${file_name// /\\ }"
        
        # Add Public Read Permission to Google Cloud objects

        eval "gsutil acl ch -u AllUsers:R gs://pollfish_production/sdk/AdMob/${slashed_file_name}"
        if [ $? -eq 0 ]; then
            echo "${file_name} is now public"
            echo "URL: https://storage.googleapis.com/pollfish_production/sdk/AdMob/${file_name// /%20}"
        else
            echo "Failed to add public access permission to: ${file_name}"
            exit 1
        fi

    done

else
    echo "Skipping Publishing"
fi

if [ "$keep_open" = false ] ; then

    # Keep Sonatype staging repository open and do not release

    if ./gradlew :closeAndReleaseRepository; then 
        echo "Closing Maven repository task succeeded"
    else
        echo "Closing Maven repository task failed"
        exit 1
    fi

else
    echo "Skipping Close/Release Maven Repository"
fi
