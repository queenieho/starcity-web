#!/usr/bin/env bash
mkdir resources/public/assets/css
echo "Installing external JS & CSS dependencies via Bower..."
bower install
echo "Compiling Materialize Sass..."
sass resources/public/assets/scss/materialize/main.scss:resources/public/assets/css/materialize.css
echo "Compiling Custom Sass..."
sass resources/public/assets/stylesheets/main.scss:resources/public/assets/css/starcity.css
