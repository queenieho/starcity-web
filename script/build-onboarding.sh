#!/usr/bin/env bash
echo "Compiling Elm Onboarding App..."
elm-make elm/Onboarding.elm --yes --output resources/public/js/elm/onboarding.js
