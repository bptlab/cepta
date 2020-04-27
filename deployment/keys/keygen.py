# -*- coding: utf-8 -*-

"""Main module."""

import glob
import time
import os
import json
import platform
import shutil
import subprocess
import tempfile
import typing
import sys
import click

def print_command(
    *cmd_args: typing.Any, verbosity: int = 0, **cmd_kwargs: typing.Any
) -> str:
    try:
        output = subprocess.check_output(*cmd_args, **cmd_kwargs).decode("utf-8")
        if verbosity > 1:
            print(output)
        return output
    except subprocess.CalledProcessError as e:  # pragma: no cover
        print(e.returncode)
        print(e.output)
        raise


def quote(s: str) -> str:
    return '"' + s + '"'


def gen_keys(
    output_file: str,
    verbosity: int = 0,
) -> None:
    abs_output = os.path.abspath(output_file)
    private_key_out = abs_output + ".key"
    public_key_out = private_key_out + ".pub"
    jwk_out = abs_output + ".jwk.json"

    for file_path in [public_key_out, private_key_out, jwk_out]:
        if os.path.exists(file_path) and os.path.isfile(file_path):
            raise ValueError(f"File named {file_path} already exists! Please remove first...")
        directory = os.path.dirname(file_path)
        if not os.path.exists(directory):
            os.makedirs(directory)
    
    # Generate key
    print("Generating RS256 key-pair...")
    cmd = f'yes "" | ssh-keygen -q -N "" -t rsa -b 4096 -m PEM -f {private_key_out}'
    if verbosity > 1:
        print(cmd)
    print_command(
        cmd,
        stderr=subprocess.STDOUT,
        shell=True,
        verbosity=verbosity,
    )

    print("Converting rsa to jwk")
    cmd = f"npx ssh-to-jwk {public_key_out} > {jwk_out}"
    if verbosity > 1:
        print(cmd)
    print_command(
        cmd,
        stderr=subprocess.STDOUT,
        shell=True,
        verbosity=verbosity,
    )

    time.sleep(1)

    with open(jwk_out, "r") as f:
        basic_jwt = json.load(f)

    # Add additional info
    basic_jwt.update(dict(
        kid="0",
        alg="RS256"
    ))

    # Wrap in 
    jwt = dict(keys=[basic_jwt])
    print(json.dumps(jwt, indent=4, sort_keys=True))

    with open(jwk_out, "w") as f:
        # Write final jwt
        json.dump(jwt, f, indent=4, sort_keys=False)

@click.command()
@click.argument("output_file", type=click.Path())
@click.option(
    "--verbosity",
    default=1,
    help=("level of verbosity when printing to stdout (the higher the more output)"),
)
def main(
    output_file: str,
    verbosity: int,
) -> int:
    try:
        gen_keys(output_file, verbosity)
    except Exception as e:  # pragma: no cover
        raise click.ClickException(str(e))
    return 0


if __name__ == "__main__":
    sys.exit(main())  # pragma: no cover