#!/usr/bin/env bash
mkdir resources/public/assets/css
echo "Installing external JS & CSS dependencies via Bower..."
bower install
echo "Installing NPM dependencies..."
npm install
echo "Compiling `ant design` LESS..."
lessc --clean-css style/less/antd.less resources/public/assets/css/antd.css
echo "Compiling SASS..."
sass style/sass/main.sass:resources/public/assets/css/starcity.css --style compressed
