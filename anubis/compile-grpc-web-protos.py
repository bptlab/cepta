import argparse
import platform
import tempfile
import shutil
import glob
import os

protoc_version = "3.11.1"
grpc_web_plugin_version = "1.0.7"
protoc_release_base_url = \
    "https://github.com/protocolbuffers/protobuf/releases/download"
grpc_web_plugin_release_base_url = \
    "https://github.com/grpc/grpc-web/releases/download"


def valid_dir(_parser, arg):
    if os.path.exists(arg) and os.path.isdir(arg):
        return arg
    _parser.error('Directory "' + arg + '" does not exist.')


def proto_compile(proto_source_dir, output_dir, js_out_options=None,
                  grpc_web_out_options=None, clear_output_dir=False):

    abs_source = os.path.abspath(proto_source_dir)
    abs_output = os.path.abspath(output_dir)
    proto_files = glob.glob(abs_source + '/*.proto')
    if not len(proto_files) > 0:
        raise AssertionError(
            "Source directory " + abs_source + " must contain .proto file(s)")

    with tempfile.TemporaryDirectory() as tmp_dirname:

        # Download correct files
        system = platform.system().lower()  # darwin
        system_alias = "osx" if system == "darwin" else system  # osx for darwin
        machine_arch = platform.machine()

        protoc_release_url = protoc_release_base_url
        protoc_release_url += "/v" + protoc_version
        protoc_release_url += "/protoc-" + protoc_version + \
            "-" + system_alias + "-" + machine_arch + ".zip"

        grpc_web_plugin_release_url = grpc_web_plugin_release_base_url
        grpc_web_plugin_release_url += "/" + grpc_web_plugin_version
        grpc_web_plugin_release_url += "/protoc-gen-grpc-web-" + \
            grpc_web_plugin_version + "-" + system + "-" + machine_arch

        download = dict(
            protoc=protoc_release_url,
            protoc_gen_grpc_web=grpc_web_plugin_release_url)

        for executable, url in download.items():
            is_zip = os.path.splitext(url)[1].lower() == "zip"
            print(
                os.popen(
                    "wget -O " +
                    tmp_dirname +
                    "/" +
                    executable +
                    " " +
                    url).read())
            if is_zip:
                print(
                    os.popen(
                        "unzip " +
                        tmp_dirname +
                        "/" +
                        executable).read())
            print(os.popen("ls -la " + tmp_dirname).read())

        # Construct command
        # See: https://github.com/grpc/grpc-web
        proto_command = "protoc -I=" + abs_source + \
            " " + str(" ").join(proto_files)

        proto_command += " --plugin=protoc-gen-grpc_web=protoc_gen_grpc_web"

        proto_command += " --js_out="
        if js_out_options:
            proto_command += js_out_options + ":"
        proto_command += abs_output

        proto_command += " --grpc-web_out="
        if grpc_web_out_options:
            proto_command += grpc_web_out_options + ":"
        proto_command += abs_output

        # Eventually clear the output dir
        if os.path.exists(abs_output) and clear_output_dir:
            shutil.rmtree(abs_output, ignore_errors=True)

        # Make sure the output path exists
        if not os.path.exists(abs_output):
            os.makedirs(abs_output)

        print(os.popen(proto_command).read())


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "proto_source_dir",
        type=lambda x: valid_dir(parser, x),
        help="directory containing the .proto sources to be compiled"
    )
    parser.add_argument(
        "output_dir",
        type=str,
        help="output directory for the compiled stubs"
    )
    parser.add_argument(
        "--js_out_options",
        dest="js_out_options",
        default="import_style=commonjs,binary",
        help="options for the javascript proto compiler"
    )
    parser.add_argument(
        "--grpc_web_out_options",
        dest="grpc_web_out_options",
        default="import_style=typescript,mode=grpcwebtext",
        help="options for the grpc web proto compiler"
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
