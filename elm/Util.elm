module Util exposing (..)

import Date exposing (Date)
import Date.Extra as Date

humanDate : Date -> String
humanDate date =
    Date.toFormattedString "EEEE, MMMM d, y 'at' h:mm a" date
