#!/usr/bin/env bash
lein clean
lein with-profile production uberjar
