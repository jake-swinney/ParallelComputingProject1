# Generate 40 text files, each 1-4 KB in size, in the tests/ directory.

import string
import random

punc = ['.', '.', '.', '?', '!']
sep = [' ', ' ', '\n']


def make_sentence():
    """
    Make a super legit sentence with 5-10 words containing 1-10 random letters.
    Uppercase the first letter and add a punctuation mark.
    Add either a space or a newline after.
    """
    words = [''.join(
            random.choices(string.ascii_lowercase, k=random.randint(1, 10))
        ) for i in range(random.randint(5, 20))]
    s = ' '.join(words)
    s = s[0].upper() + s[1:]
    s += random.choice(punc) + random.choice(sep)
    return s


def make_text():
    out = ''
    size_threshold = random.randint(1000, 4000)
    while len(out) < size_threshold:
        out += make_sentence()
    return out[:-1]


def main():
    for i in range(1, 41):
        with open(f"tests/text{i}.txt", 'w') as f:
            f.write(make_text())


if __name__ == "__main__":
    main()
