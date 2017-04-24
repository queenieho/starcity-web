#!/usr/bin/env bash
mkdir resources/public/assets/css
echo "Installing NPM dependencies..."
npm install
echo "Compiling ant design LESS..."
lessc --clean-css style/less/antd.less resources/public/assets/css/antd.css
echo "Compiling public-facing SASS..."
sass -E "UTF-8" style/sass/public.scss:resources/public/assets/css/public.css
echo "Compiling internal SASS..."
# TODO: Rename starcity.css to something else
sass -E "UTF-8" style/sass/main.sass:resources/public/assets/css/starcity.css --style compressed
