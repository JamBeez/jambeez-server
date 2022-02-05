package com.github.jambeez.server.worker

// Lobby Related
const val LOBBY = "lobby"
const val LOBBY_CREATE = "lobby:create"
const val LOBBY_JOIN = "lobby:join"
const val LOBBY_UPDATE_PARTS = "lobby:update_parts"

// User Related
const val USER = "user"
const val USER_CHANGE_ALIAS = "user:change_alias"
const val USER_JOINED = "user:joined"

// Part Related
const val PART = "part"
const val PART_CHANGE_BPM = "part:change_bpm"
const val PART_CHANGE_BARS = "part:change_bars"
const val PART_CHANGE_SIG_LOWER = "part:change_sig_lower"
const val PART_CHANGE_SIG_UPPER = "part:change_sig_upper"

// Track Related
const val TRACK = "track"
const val TRACK_TOGGLE_MUTE = "track:toggle_mute"
const val TRACK_SET_SAMPLE = "track:set_sample"
const val TRACK_SET_BEATS = "track:set_beats"