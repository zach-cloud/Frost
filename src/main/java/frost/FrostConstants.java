package frost;

public interface FrostConstants {

    /** Computed by taking the hashAsInt("(block table)", 3) */
    int BLOCK_TABLE_ENCRYPTION_KEY = -326913117;

    /** Computed by taking the hashAsInt("(hash table)", 3) */
    int HASH_TABLE_ENCRYPTION_KEY = -1011927184;

    /** MPQ Contract defined constants */
    int MPQ_HASH_ENTRY_EMPTY = (int)0xFFFFFFFFL;
    int MPQ_HASH_ENTRY_DELETED = (int)0xFFFFFFFEL;

    int MPQ_HASH_TABLE_OFFSET = 0;
    int MPQ_HASH_NAME_A = 1;
    int MPQ_HASH_NAME_B = 2;
    int MPQ_HASH_FILE_KEY = 3;

    int BYTES_PER_BLOCK_TABLE_ENTRY = 16;
    int BYTES_PER_HASH_TABLE_ENTRY = 16;

    /** Frost external constants */
    short ANY_PLATFORM = -2;
    short ANY_LANGUAGE = -2;
}
