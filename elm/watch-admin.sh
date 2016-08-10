#!/bin/bash
fswatch -o admin | elm-make admin/Main.elm --output ../resources/public/js/admin.js
