/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.ai;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * xor neural network demo
 * demo shows how to recognize numbers for idcard
 * image should be 680 * 430
 * @author lijiaming
 *
 */
public class Demo {
	public static final String saveTo = "/Users/mingo/work/code/files/png/idcard-model-352x50x10.ser";
	public static final String saveYesNoTo = "/Users/mingo/work/code/files/png/idcard-yes-no-model-352x50x2.ser";
	public static void main(String[] args) throws Exception {
//		trainImg();
//		trainYesNo();
		testImg();
	}
	public static AtomicInteger lastNum = new AtomicInteger(0);
	public static double[] lastPro = new double[1];
	public static void testImg() throws IOException {
		File file = new File("/Users/mingo/Downloads/id9.png");
		int[][] image = Util.readImage(file);
		System.out.println(image.length);
		System.out.println(image[0].length);
		System.out.println("start to do recognition?[y/N]");
		NN nn = (NN)Util.deserialize(saveTo);
		NN yesno = (NN)Util.deserialize(saveYesNoTo);
		System.out.println(nn);
		FrameNormalizer normalizer = new FrameNormalizer();
		FrameSlider lineSlider = new FrameSlider(image, image.length,28,1,1, 0, 0);
		StreamSupport.stream(lineSlider, false).forEach(line->{
			int[][] row = line.data;
			List<Integer> list = new ArrayList<>();
			//for one line
			FrameSlider frameSlider = new FrameSlider(row, 18,24,1,1, 0, 0);
			StreamSupport.stream(frameSlider, false)
					.forEach(f18x24 -> {
						FrameSlider dotSlider = new FrameSlider(f18x24.data, 3, 3, 2, 2, 0, 0);
						List<Double> collect = StreamSupport.stream(dotSlider, false)
								.map(normalizer)
								.flatMap(Collection::stream)
								.collect(Collectors.toList());
						Data data = new Data(collect, new double[0]);
						double[] test = yesno.test(data.x);
						int index = Util.argmax(test);

						if (index == 0) {
							double[] result = nn.test(data.x);
							int num = Util.argmax(result);
							if(result[num] > 0.6) {
								list.add(num);
//								String filename = String.format("frame_%d-x%d.jpg", num, f18x24.x);
//								Util.printImage(f18x24.data, "/Users/mingo/work/code/files/png/idcard-predict/" + filename);
							}
						}
					});

			if(list.size() > 50) {
				System.out.println(list);
				list.clear();
				List<Frame> idcard = new ArrayList<>();
				AtomicInteger next = new AtomicInteger(0);
				while(next.get() < image.length) {
					FrameSlider idnum = new FrameSlider(row, 18,24,1,1, next.get(), 0);
					Optional<Frame> first = StreamSupport.stream(idnum, false)
							.filter(f18x24 -> {
								FrameSlider dotSlider = new FrameSlider(f18x24.data, 3, 3, 2, 2, 0, 0);
								List<Double> collect = StreamSupport.stream(dotSlider, false)
										.map(normalizer)
										.flatMap(Collection::stream)
										.collect(Collectors.toList());
								Data data = new Data(collect, new double[0]);
								double[] test = yesno.test(data.x);
								int index = Util.argmax(test);

								if (index == 0) {
									double[] result = nn.test(data.x);

									int num = Util.argmax(result);
									if (result[num] > 0.6) {
										System.out.println("here is the number: " + num + " which probability is " + result[num]);
										next.set(f18x24.x);
										idcard.add(f18x24.setY(num));
										return true;
									}
								}
								return false;
							}).findAny();
					if(first.isPresent()) {
						next.addAndGet(16);
					} else {
						next.addAndGet(1);
					}
				}
				System.out.println(list);
				if(idcard.size() == 18) {
					idcard.forEach(data -> {
						String filename = String.format("frame_%d-x%d.jpg", data.y, data.x);
						Util.printImage(data.data, "/Users/mingo/work/code/files/png/idcard-predict/" + filename);
					});

					System.exit(-1);
				}
			}
		});
	}

	public static void trainYesNo() throws IOException {
		FrameNormalizer normalizer = new FrameNormalizer();
		List<Data> all = new ArrayList<>();
		List<Data> yes = new ArrayList<>();
		List<Data> no = new ArrayList<>();
		Files.list(Paths.get("/Users/mingo/work/code/files/png/idcard-learn"))
				.filter(file->file.toFile().isDirectory())
				.forEach(dir -> {
					try {
						List<Data> datas = Files.list(dir)
								.map(Path::toFile)
								.filter(file -> file.getName().endsWith("jpg"))
								.map(f->{
									int[][] image = Util.readImage(f);
									List<Double> collect = StreamSupport.stream(new FrameSlider(image, 3, 3, 2, 2, 0, 0), false)
											.map(normalizer)
											.flatMap(Collection::stream)
											.collect(Collectors.toList());
									return new Data(collect, Util.onehot(0, 2));
								})
								.collect(Collectors.toList());
						yes.addAll(datas);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
		List<Data> datas = Files.list(Paths.get("/Users/mingo/work/code/files/png/none"))
				.map(Path::toFile)
				.filter(file -> isImage(file.getName()))
				.map(f->{
					int[][] image = Util.readImage(f);
					List<Double> collect = StreamSupport.stream(new FrameSlider(image, 3, 3, 2, 2, 0, 0), false)
							.map(normalizer)
							.flatMap(Collection::stream)
							.collect(Collectors.toList());
					return new Data(collect, Util.onehot(1, 2));
				})
				.collect(Collectors.toList());
		no.addAll(datas);

		System.out.println(yes.size());
		System.out.println(no.size());
		NN nn = (NN)Util.deserialize(saveYesNoTo);//new NN(352, 2, new int[]{50});//
		for(int i=0; i<100;i++) {
			Collections.shuffle(no);
			List<Data> same = no.subList(0, yes.size());
			all.clear();
			all.addAll(yes);
			all.addAll(same);
			Collections.shuffle(all);
			double error = 0;
			for(Data data : all) {
				error +=nn.train(data.x, data.y);
			};
			if(error < 0.001) {
				System.out.println("small enough");
				break;
			}
			System.out.println(i + "\t" + error/all.size());
			Util.serialize(nn, saveYesNoTo);
		}
	}

	public static void trainImg() throws IOException {
		FrameNormalizer normalizer = new FrameNormalizer();
		List<Data> all = new ArrayList<>();
		Files.list(Paths.get("/Users/mingo/work/code/files/png/idcard-learn"))
				.filter(file->file.toFile().isDirectory())
				.forEach(dir -> {
					try {
						String name = dir.toFile().getName();
						Integer index = Integer.parseInt(name);
						List<Data> datas = Files.list(dir)
								.map(Path::toFile)
								.filter(file -> isImage(file.getName()))
								.map(f->{
									int[][] image = Util.readImage(f);
									List<Double> collect = StreamSupport.stream(new FrameSlider(image, 3, 3, 2, 2, 0, 0), false)
											.map(normalizer)
											.flatMap(Collection::stream)
											.collect(Collectors.toList());
									return new Data(collect, Util.onehot(index, 10));
								})
								.collect(Collectors.toList());
						all.addAll(datas);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
		System.out.println(all.size());
		System.out.println(all.get(0));
		NN nn = (NN)Util.deserialize(saveTo);//new NN(352, 10, new int[]{50});//
		for(int i=0; i<100;i++) {
			Collections.shuffle(all);
			double error = 0;
			for(Data data : all) {
				error +=nn.train(data.x, data.y);
			};
			System.out.println(i + "\t" + error/all.size());
			Util.serialize(nn, saveTo);
		}
	}

	public static boolean isImage(String name) {
		return name.endsWith("jpg")||name.endsWith("png")||name.endsWith("jpeg");
	}
	public static void readImg(){
		File file = new File("/Users/mingo/Downloads/id2.png");
		int[][] image = Util.readImage(file);
		FrameNormalizer normalizer = new FrameNormalizer();
		FrameSlider slider = new FrameSlider(image, 18,24,3,3, 0, 0);
		StreamSupport.stream(slider, false)
				.forEach(f18x24 -> {
					Util.printImage(f18x24.getData(), "/Users/mingo/work/code/files/png/idcard3/frame-" + f18x24.x + "_" + f18x24.y + ".jpg");
				});
	}

	public static void testXOR(){
		AI ai = null;
		String file = "xor.ai.ser";
		if(new File(file).exists()) {
			System.out.println("deserialize from existing file");
			ai = (AI) Util.deserialize(file);
		} else {
			System.out.println("create new ai object");
			ai = new NN(2,2, new int[] {5,5});
		}
		
		double[][] inputs = new double[4][2];
		double[][] outputs = new double[4][2];
		inputs[0] = new double[]{0,0};
		inputs[1] = new double[]{0,1}; 
		inputs[2] = new double[]{1,0}; 
		inputs[3] = new double[]{1,1}; 
		outputs[0] = new double[]{1,0};
		outputs[1] = new double[]{0,1}; 
		outputs[2] = new double[]{0,1}; 
		outputs[3] = new double[]{1,0}; 
		System.out.println("train until converge");
		ai.train(inputs, outputs);
		System.out.println("serialize ai ser to file");
		Util.serialize(ai, file);
	}
}
