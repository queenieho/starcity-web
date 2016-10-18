#!/usr/bin/env bash
echo "Compiling Elm Dashboard App..."
elm-make elm/dashboard.elm --yes --output resources/public/js/elm/dashboard.js
