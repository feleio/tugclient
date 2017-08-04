package com.babylonhealth.tugclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import protos.TugGrpc;
import protos.TugGrpc.TugBlockingStub;
import protos.TugOuterClass.ConnectReply;
import protos.TugOuterClass.ConnectRequest;
import protos.TugOuterClass.GetStateReply;
import protos.TugOuterClass.GetStateRequest;
import protos.TugOuterClass.PullReply;
import protos.TugOuterClass.PullRequest;
import protos.TugOuterClass.ResetReply;
import protos.TugOuterClass.ResetRequest;

public class TugClient {
  private final int ROPE_LENGTH = 51;
  private final ManagedChannel channel;
  private final TugBlockingStub blockingStub;
  public final static String USER_NAME = "felix";

  public class RenderRunnable implements Runnable {
    public void run() {
      while(true) {
        GetStateRequest request = GetStateRequest.newBuilder().build();
        GetStateReply reply = blockingStub.getState(request);
        try {
          render(reply.getCurrentPosition());

        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    private void render(int currentPosition) throws IOException {
      String initStr = new String(new char[ROPE_LENGTH]).replace('\0', '-');
      StringBuilder res = new StringBuilder("\r " + initStr);
      res.setCharAt(currentPosition, '#');
      //Runtime.getRuntime().exec("cls");
      System.out.print(res.toString());
    }
  }

  public class EnterKeyRunnable implements Runnable {
    BufferedReader br=new BufferedReader(new InputStreamReader(System.in));

    public void run() {
      while (true) {
        try {
          br.readLine();
          PullRequest request = PullRequest.newBuilder().setUserId(USER_NAME).build();
          blockingStub.pull(request);

        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public TugClient(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
  }

  public TugClient(ManagedChannelBuilder<?> channelBuilder) {
    channel = channelBuilder.build();
    blockingStub = TugGrpc.newBlockingStub(channel);
  }

  public ConnectReply connect(String name) {
    ConnectRequest request = ConnectRequest.newBuilder().setName(name).build();
    return blockingStub.connect(request);
  }

  public PullReply pull(){
    PullRequest request = PullRequest.newBuilder().build();
    return blockingStub.pull(request);
  }

  public ResetReply reset() {
    ResetRequest request = ResetRequest.newBuilder().build();
    return blockingStub.reset(request);
  }

  public static void main(String[] args) throws InterruptedException {
    TugClient client = new TugClient("192.168.4.72", 7777);
    ConnectReply reply = client.connect(TugClient.USER_NAME);

    System.out.print(reply);
    Thread renderThread = (new Thread(client.new RenderRunnable()));
    Thread enterKeyThread = (new Thread(client.new EnterKeyRunnable()));

    renderThread.start();
    enterKeyThread.start();

    renderThread.join();
    enterKeyThread.join();
  }

  protected void finalize() throws Throwable {










    channel.shutdown();
    super.finalize();
  }
}
