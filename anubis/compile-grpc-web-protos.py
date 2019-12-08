import argparse
import platform
import tempfile
import shutil
import glob
import os
import subprocess
import sys

reload(sys)
sys.setdefaultencoding("utf-8")

protoc_version = "3.11.1"
grpc_web_plugin_version = "1.0.7"
protoc_release_base_url = (
    "https://github.com/protocolbuffers/protobuf/releases/download"
)
grpc_web_plugin_release_base_url = "https://github.com/grpc/grpc-web/releases/download"


def valid_dir(_parser, arg):
    if os.path.exists(arg) and os.path.isdir(arg):
        return arg
    _parser.error('Directory "' + arg + '" does not exist.')


def print_command(*cmd_args, **cmd_kwargs):
    try:
        print(subprocess.check_output(*cmd_args, **cmd_kwargs).decode("utf-8"))
    except subprocess.CalledProcessError as e:
        print(e.returncode)
        print(e.output)
        raise


def quote(s):
    return '"' + s + '"'


def proto_compile(
    proto_source_dir,
    output_dir,
    js_out_options=None,
    grpc_web_out_options=None,
    clear_output_dir=False,
):
    abs_source = os.path.abspath(proto_source_dir)
    abs_output = os.path.abspath(output_dir)
    proto_files = glob.glob(abs_source + "/*.proto")
    if not len(proto_files) > 0:
        raise AssertionError(
            "Source directory " + abs_source + " must contain .proto file(s)"
        )

    tmp_dir = tempfile.mkdtemp()
    try:
        # Download correct files
        system = platform.system().lower()  # darwin
        system_alias = "osx" if system == "darwin" else system  # osx for darwin
        machine_arch = platform.machine()

        protoc_release_url = protoc_release_base_url
        protoc_release_url += "/v" + protoc_version
        protoc_release_url += (
            "/protoc-"
            + protoc_version
            + "-"
            + system_alias
            + "-"
            + machine_arch
            + ".zip"
        )

        grpc_web_plugin_release_url = grpc_web_plugin_release_base_url
        grpc_web_plugin_release_url += "/" + grpc_web_plugin_version
        grpc_web_plugin_release_url += (
            "/protoc-gen-grpc-web-"
            + grpc_web_plugin_version
            + "-"
            + system
            + "-"
            + machine_arch
        )

        download = dict(
            protoc=protoc_release_url, myprotoc=grpc_web_plugin_release_url
        )

        for executable, url in download.items():
            filename = os.path.basename(url)
            is_zip = os.path.splitext(filename)[1].lower() == ".zip"
            download_command = str(" ").join(
                [
                    "wget",
                    "-O",
                    tmp_dir + "/" + (filename if is_zip else executable),
                    quote(url),
                ]
            )
            print(download_command)
            print_command(download_command, stderr=subprocess.STDOUT, shell=True)
            if is_zip:
                unzip_command = str(" ").join(
                    [
                        "unzip",
                        tmp_dir + "/" + filename,
                        "-d",
                        tmp_dir + "/" + executable,
                    ]
                )
                print(unzip_command)
                print_command(unzip_command, stderr=subprocess.STDOUT, shell=True)

        # Make executables
        for executable in [tmp_dir + "/protoc/bin/protoc", tmp_dir + "/myprotoc"]:
            chmod_command = str(" ").join(
                ["chmod", "+x", executable]
            )
            print(chmod_command)
            print_command(chmod_command, stderr=subprocess.STDOUT,
                          shell=True)

        print_command("ls -la " + tmp_dir, stderr=subprocess.STDOUT, shell=True)

        # Construct protoc compiler command
        # See: https://github.com/grpc/grpc-web
        proto_command = [
            tmp_dir + "/protoc/bin/protoc",
            "-I=" + abs_source,
        ] + proto_files
        proto_command.append(
            "--plugin=protoc-gen-grpc_web=" + tmp_dir + "/myprotoc"
        )

        proto_command.append(
            "--js_out="
            + ((js_out_options + ":") if js_out_options else "")
            + abs_output
        )

        proto_command.append(
            "--grpc-web_out="
            + ((grpc_web_out_options + ":") if grpc_web_out_options else "")
            + abs_output
        )

        # Eventually clear the output dir
        if os.path.exists(abs_output) and clear_output_dir:
            shutil.rmtree(abs_output, ignore_errors=True)

        # Make sure the output path exists
        if not os.path.exists(abs_output):
            os.makedirs(abs_output)

        print(str(" ").join(proto_command))
        print_command(str(" ").join(proto_command), stderr=subprocess.STDOUT, shell=True)

    finally:
        # Remove temporary directory
        shutil.rmtree(tmp_dir, ignore_errors=True)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "proto_source_dir",
        type=lambda x: valid_dir(parser, x),
        help="directory containing the .proto sources to be compiled",
    )
    parser.add_argument(
        "output_dir", type=str, help="output directory for the compiled stubs"
    )
    parser.add_argument(
        "--js_out_options",
        dest="js_out_options",
        default="import_style=commonjs,binary",
        help="options for the javascript proto compiler",
    )
    parser.add_argument(
        "--grpc_web_out_options",
        dest="grpc_web_out_options",
        default="import_style=typescript,mode=grpcwebtext",
        help="options for the grpc web proto compiler",
    )
    parser.add_argument(
        "--clear_output_dir",
        dest="clear_output_dir",
        default=False,
        action="store_true",
        help="whether to clear the output directory before compilation",
    )
    args = parser.parse_args()
    proto_compile(**vars(args))
