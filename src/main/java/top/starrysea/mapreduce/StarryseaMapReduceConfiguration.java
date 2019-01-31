package top.starrysea.mapreduce;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class StarryseaMapReduceConfiguration {

	private String inputPath;
	private String outputPath;

	private int mapperCorePoolSize;
	private int mapperMaximumPoolSize;
	private RejectedExecutionHandler mapperRejectedExecutionHandler;

	private int reducerCorePoolSize;
	private int reducerMaximumPoolSize;
	private RejectedExecutionHandler reducerRejectedExecutionHandler;

	private StarryseaMapReduceConfiguration() {
		this.mapperCorePoolSize = Runtime.getRuntime().availableProcessors();
		this.mapperMaximumPoolSize = 10;
		this.mapperRejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
		this.reducerCorePoolSize = Runtime.getRuntime().availableProcessors();
		this.reducerMaximumPoolSize = 10;
		this.reducerRejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
	}

	public static StarryseaMapReduceConfiguration of() {
		return new StarryseaMapReduceConfiguration();
	}

	public StarryseaMapReduceConfiguration input(String input) {
		this.inputPath = input;
		return this;
	}

	public StarryseaMapReduceConfiguration output(String output) {
		this.outputPath = output;
		return this;
	}

	public StarryseaMapReduceConfiguration mapperCorePoolSize(int mapperCorePoolSize) {
		this.mapperCorePoolSize = mapperCorePoolSize;
		return this;
	}

	public StarryseaMapReduceConfiguration mapperMaximumPoolSize(int mapperMaximumPoolSize) {
		this.mapperMaximumPoolSize = mapperMaximumPoolSize;
		return this;
	}

	public StarryseaMapReduceConfiguration mapperRejectedExecutionHandler(
			RejectedExecutionHandler mapperRejectedExecutionHandler) {
		this.mapperRejectedExecutionHandler = mapperRejectedExecutionHandler;
		return this;
	}

	public StarryseaMapReduceConfiguration reducerCorePoolSize(int reducerCorePoolSize) {
		this.reducerCorePoolSize = reducerCorePoolSize;
		return this;
	}

	public StarryseaMapReduceConfiguration reducerMaximumPoolSize(int reducerMaximumPoolSize) {
		this.reducerMaximumPoolSize = reducerMaximumPoolSize;
		return this;
	}

	public StarryseaMapReduceConfiguration reducerRejectedExecutionHandler(
			RejectedExecutionHandler reducerRejectedExecutionHandler) {
		this.reducerRejectedExecutionHandler = reducerRejectedExecutionHandler;
		return this;
	}

	public String getInputPath() {
		return inputPath;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public int getMapperCorePoolSize() {
		return mapperCorePoolSize;
	}

	public int getMapperMaximumPoolSize() {
		return mapperMaximumPoolSize;
	}

	public int getReducerCorePoolSize() {
		return reducerCorePoolSize;
	}

	public int getReducerMaximumPoolSize() {
		return reducerMaximumPoolSize;
	}

	public RejectedExecutionHandler getMapperRejectedExecutionHandler() {
		return mapperRejectedExecutionHandler;
	}

	public RejectedExecutionHandler getReducerRejectedExecutionHandler() {
		return reducerRejectedExecutionHandler;
	}

}
