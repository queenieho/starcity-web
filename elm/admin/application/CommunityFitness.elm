module Admin.Application.CommunityFitness exposing (..)

import Html exposing (..)
import Material.List as Lists
import Json.Decode exposing (Decoder, string, maybe, (:=), object5)
import Admin.Application.Common as Common

-- MODEL

type alias CommunityFitness =
    { whyInterested : String
    , priorCommunityHousing : String
    , skills : String
    , freeTime : String
    , dealbreakers : Maybe String
    }

-- VIEW


view : CommunityFitness -> Html a
view cf =
    Common.section "Community Fitness"
        <| Common.definitionList
            [ ("Have you ever lived in communal housing?", cf.priorCommunityHousing)
            , ("What skills or traits do you hope to share with the community?", cf.skills)
            , ("Why are you interested in Starcity?", cf.whyInterested)
            , ("How do you spend your free time?", cf.freeTime)
            , ("Do you have any dealbreakers?", Maybe.withDefault "None" cf.dealbreakers)
            ]

-- JSON DECODER

decoder : Decoder CommunityFitness
decoder =
    object5 CommunityFitness
        ("why_interested" := string)
        ("prior_community_housing" := string)
        ("skills" := string)
        ("free_time" := string)
        (maybe ("dealbreakers" := string))
