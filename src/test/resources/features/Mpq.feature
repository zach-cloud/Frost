Feature: MPQ end to end tests

  Scenario: Test reading basic unprotected MPQ
    Given File is deleted: "saved.w3x"
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
    Given a real file writer
    When File is added "test.txt"
    When File is saved as "saved.w3x"
    Then File should exist on disk "saved.w3x"
    Given MPQ file: "saved.w3x"
    When MPQ file is read
    Then File should exist: "test.txt"
    Given File is deleted: "saved.w3x"

  Scenario: Test reading basic protected map
    Given MPQ file: "VampirismSpeed.w3x"
    When MPQ file is read
    Then MPQ should have 82 total files
    Then MPQ should have 82 known files
    # There are actually 2 empty files.
    Then MPQ should have 0 unknown files
    Then File should exist: "Ls3.blp"
    When File is extracted: "Scripts\war3map.j"
    Then File should have been extracted: "Scripts\war3map.j"
    When File is extracted: "DoesNotExist.txt"
    Then 0 files should have been extracted
    When All known files are extracted
    Then 82 files should have been extracted
    When File names are retrieved
    Then There should be 82 file names
    When All known files are extracted with listfile "listfile.txt"
    Then 84 files should have been extracted

  Scenario: Test reading complex protected map
    Given File is deleted: "saved.w3x"
    Given MPQ file: "Pg.w3x"
    When MPQ file is read
    When File is added "test.txt"
    When File is saved as "saved.w3x"
    Then File should exist on disk "saved.w3x"
    Given MPQ file: "saved.w3x"
    When MPQ file is read
    Then File should exist: "test.txt"
    Given File is deleted: "saved.w3x"