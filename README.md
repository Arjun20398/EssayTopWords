# Fetch top words from 40k crawled essays
We will be fetching the top k words from all the essays given in [file](https://drive.google.com/file/d/1TF4RPuj8iFwpa-lyhxG67V8NDlktmTGi/view/) 

A valid word is:
1. Contain at least 3 characters.
2. Contain only alphabetic characters.
3. Be part of our bank of [words](https://raw.githubusercontent.com/dwyl/english-words/master/words.txt) (not all the words in the bank are valid according to the
previous rules)

# High-level design

![FireFly](https://github.com/Arjun20398/EssayTopWords/assets/24750890/aa1ba7c7-c460-422f-b6f3-2ea09f39710c)

## Read file having 40k urls
1. Read url file online
2. Converted into chunks of API_CALL_CHUNK_SIZE

## Read essays chunk by chunk
1. Get a chunk from the previous step
2. Use ExponentialBackOffQueueProcessor for reading from Engadget
3. Validate if words are valid or not
4. Return Map<String, Long> map for this chunk

## Merge all chunks
1. Merge all maps from chunk
2. Return the top k words


## Tech-Stack
1. Java, Spring-Boot, Spring-Batching

## ExponentialBackOffLogic
![ExponentialBackoff](https://github.com/Arjun20398/EssayTopWords/assets/24750890/210d993c-8f8d-497d-be3e-ce8c62d1844c)
