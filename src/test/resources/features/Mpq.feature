Feature: MPQ end to end tests

  Scenario: Test reading basic unprotected MPQ
    Given MPQ file: "JungleEscape.w3x"
    When MPQ file is read
    Then MPQ should have 19 total files
    Then MPQ should have 19 known files
    Then MPQ should have 0 unknown files
    Then File should exist: "war3map.wts"
    When File is extracted: "war3map.j"
    Then File should have been extracted: "war3map.j"
    When File is extracted: "DoesNotExist.txt"
    Then 0 files should have been extracted
    When All known files are extracted
    Then 19 files should have been extracted
    When File names are retrieved
    Then There should be 19 file names
    When All known files are extracted with listfile "listfile.txt"
    Then 19 files should have been extracted
