# The ONE

The Opportunistic Network Environment simulator.

For introduction and releases, see [the ONE homepage at GitHub](http://akeranen.github.io/the-one/).

For instructions on how to get started, see [the README](https://github.com/akeranen/the-one/wiki/README).

The [wiki page](https://github.com/akeranen/the-one/wiki) has the latest information.

## Getting Started: FMI Building

1. Compile the simulator into a JAR
2. The next step assumes the JAR is located in the project root and is named `the_one.jar`
3. Run `$ python3 ./batch_runner.py ./the_one.jar ./university_settings ./reports ./data` to run all settings.  

See `batch_runner.py` for detailed usage information.

Heat maps can be generated with `$ jupyter notebook ./heat_maps.ipynb`. This requires Jupyter with a Python 3 kernel to be installed. 
`numpy` and `matplotlib` have to be installed in the Python environment.
