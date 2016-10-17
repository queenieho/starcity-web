#!/usr/bin/env bash
mkdir resources/public/assets/css
echo "Installing external JS & CSS dependencies via Bower..."
bower install
echo "Compiling SASS..."
sass resources/public/assets/stylesheets/main.scss:resources/public/assets/css/starcity.css
