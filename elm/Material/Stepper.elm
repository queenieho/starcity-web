module Material.Stepper
    exposing
        ( stepper
        , horizontal
        , vertical
        , linear
        , nonlinear
        , activeStep
        , Step
        , step
        , completed
        , error
        , title
        , titleMessage
        , editable
        , optional
        , onClick
          --  Button Helpers
        , next
        , back
        , skip
        , cancel
        )

import Html exposing (Html, Attribute)
import Html.Events as Html
import Material.Icon as Icon
import Material.Options as Options exposing (Property, cs, css, when, nop)
import Material.Button as Button


-- Stepper


type Orientation
    = Horizontal
    | Vertical


type StepperType
    = Linear
    | Nonlinear


type alias Stepper =
    { activeStep : Int
    , orientation : Orientation
    , stepperType : Maybe StepperType
    }


defaultStepper : Stepper
defaultStepper =
    { activeStep = 0
    , orientation = Horizontal
    , stepperType = Nothing
    }


stepper : List (Property Stepper m) -> List (Step m) -> Html m
stepper options steps =
    let
        ({ config } as summary) =
            Options.collect defaultStepper options

        injectProps idx ( opts, content, actions ) =
            let
                opts' =
                    opts
                        ++ [ index idx
                           , active `when` (config.activeStep == idx)
                           ]
            in
                step' opts' content actions
    in
        Options.apply summary
            Html.ul
            [ cs "mdl-stepper"
            , case config.orientation of
                Horizontal ->
                    cs "mdl-stepper--horizontal"

                Vertical ->
                    cs "mdl-stepper-vertical"
            ]
            []
            (List.indexedMap injectProps steps)


horizontal : Property Stepper m
horizontal =
    Options.set <| \self -> { self | orientation = Horizontal }


vertical : Property Stepper m
vertical =
    Options.set <| \self -> { self | orientation = Vertical }


stepperType : StepperType -> Property Stepper m
stepperType t =
    Options.set <| \self -> { self | stepperType = Just t }


linear : Property Stepper m
linear =
    stepperType Linear


nonlinear : Property Stepper m
nonlinear =
    stepperType Nonlinear


{-| Set the active step.
-}
activeStep : Int -> Property Stepper m
activeStep idx =
    Options.set (\config -> { config | activeStep = idx })



-- Step


type Status
    = Completed
    | Error String


type alias StepConfig m =
    { index : Int
    , title : String
    , titleMessage : Maybe String
    , status : Maybe Status
    , active : Bool
    , editable : Bool
    , optional : Bool
    , onClick : Maybe (Attribute m)
    }


defaultStep : StepConfig m
defaultStep =
    { index = 0
    , title = ""
    , titleMessage = Nothing
    , status = Nothing
    , active = False
    , editable = False
    , optional = False
    , onClick = Nothing
    }


type alias Step m =
    ( List (Property (StepConfig m) m), List (Html m), List (Html m) )


step : List (Property (StepConfig m) m) -> List (Html m) -> List (Html m) -> Step m
step options content actions =
    ( options, content, actions )


{-| Stepper `step`
-}
step' : List (Property (StepConfig m) m) -> List (Html m) -> List (Html m) -> Html m
step' options content actions =
    let
        ({ config } as summary) =
            Options.collect defaultStep options
    in
        Options.apply summary
            Html.li
            [ cs "mdl-step"
            , cs "is-active" `when` config.active
            , cs "mdl-step--editable" `when` config.editable
            , cs "mdl-step--optional" `when` config.optional
            , case config.status of
                Just Completed ->
                    cs "mdl-step--completed"

                Just (Error _) ->
                    cs "mdl-step--error"

                Nothing ->
                    nop
            ]
            []
            [ stepLabel summary
            , Options.div
                [ cs "mdl-step__content" ]
                content
            , Options.div
                [ cs "mdl-step__actions" ]
                actions
            ]


stepLabel : Options.Summary (StepConfig m) m -> Html m
stepLabel summary =
    let
        config =
            summary.config
    in
        Options.apply summary
            Html.span
            [ cs "mdl-step__label" ]
            (config.onClick
                |> Maybe.map (flip (::) [])
                |> Maybe.withDefault []
            )
            [ stepTitle config.title config.titleMessage config.status
            , stepIndicator config.status config.editable config.index
            ]


stepTitle : String -> Maybe String -> Maybe Status -> Html a
stepTitle title message status =
    let
        stepMessage msg =
            Options.span [ cs "mdl-step__title-message" ] [ Html.text msg ]

        message' =
            case ( status, message ) of
                ( Just (Error msg), _ ) ->
                    stepMessage msg

                ( _, Just msg ) ->
                    stepMessage msg

                _ ->
                    Options.span [] []
    in
        Options.span
            [ cs "mdl-step__title" ]
            [ Options.span
                [ cs "mdl-step__title-text" ]
                [ Html.text title ]
            , message'
            ]


stepIndicator : Maybe Status -> Bool -> Int -> Html a
stepIndicator status editable index =
    let
        attrs =
            [ cs "mdl-step__label-indicator-content" ]

        wrapContent content =
            Options.span attrs [ Html.text content ]
    in
        Options.span
            [ cs "mdl-step__label-indicator" ]
            [ (case status of
                Just Completed ->
                    Icon.view
                        (if editable then
                            "edit"
                         else
                            "check"
                        )
                        attrs

                Just (Error msg) ->
                    wrapContent "!"

                Nothing ->
                    wrapContent <| toString (index + 1)
              )
            ]


onClick : m -> Property { a | onClick : Maybe (Attribute m) } m
onClick x =
    Options.set (\options -> { options | onClick = Just (Html.onClick x) })


index : Int -> Property (StepConfig m) m
index idx =
    Options.set <| \self -> { self | index = idx }


title : String -> Property (StepConfig m) m
title t =
    Options.set <| \self -> { self | title = t }


titleMessage : String -> Property (StepConfig m) m
titleMessage st =
    Options.set <| \self -> { self | titleMessage = Just st }


active : Property (StepConfig m) m
active =
    Options.set <| \self -> { self | active = True }


status : Status -> Property (StepConfig m) m
status s =
    Options.set <| \self -> { self | status = Just s }


completed : Property (StepConfig m) m
completed =
    status Completed


error : String -> Property (StepConfig m) m
error msg =
    status <| Error msg


editable : Property (StepConfig m) m
editable =
    Options.set <| \self -> { self | editable = True }


optional : Property (StepConfig m) m
optional =
    Options.set <| \self -> { self | optional = True }



-- Actions


next : Button.Property m
next =
    Options.data "stepper-next" ""


back : Button.Property m
back =
    Options.data "stepper-back" ""


cancel : Button.Property m
cancel =
    Options.data "stepper-cancel" ""


skip : Button.Property m
skip =
    Options.data "stepper-skip" ""
