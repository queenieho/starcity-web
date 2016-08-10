#!/usr/bin/env bash
mkdir resources/public/assets/css
echo "Installing external JS & CSS dependencies via Bower..."
bower install
echo "Compiling Sass..."
sass resources/public/assets/scss/main.scss:resources/public/assets/css/main.css
echo "Compiling Elm Admin app..."
elm-make elm/Admin.elm --output resources/public/js/elm/admin.js
