package com.redbubble.pricer.common

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.charset.{Charset => NioCharset}

import com.twitter.finagle.netty3.ChannelBufferBuf.Owned
import com.twitter.io.Buf

object BufOps {
  val DefaultCharset: NioCharset = UTF_8

  final def bufToByteBuffer(buf: Buf): ByteBuffer = Owned.extract(buf).toByteBuffer()

  def bufToString(buf: Buf, charset: NioCharset = DefaultCharset): String = {
    val output = new Array[Byte](buf.length)
    buf.write(output, 0)
    new String(output, charset)
  }
}
