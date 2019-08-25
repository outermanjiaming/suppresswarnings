var touchStartTime = 0
const head = 'https://suppresswarnings.com/wx.http?action=draw'
Page({
  data: {
    times: 1,
    box: {},
    moving: {
      'state': 0,
      'id': '0',
      dx: 0,
      dy: 0
    },
    canvas: {},
    frames: {}
  },
  onLoad: function(options) {
    var that = this;
    that.sys();
    console.log("hello world");
    const ctx = wx.createCanvasContext('canvas');
    that.data.canvas = ctx;
    this.interval = null;
    that.lines(ctx, 320, 320, 20, 20);
    ctx.stroke();
    that.house(ctx, 'lightblue', 20, 120, 50, 50, 'b0');
    that.house(ctx, 'lightgreen', 80, 120, 150, 50, 'b1');
    that.house(ctx, 'gray', 240, 120, 30, 50, 'b2');
    that.house(ctx, 'yellow', 20, 180, 250, 50, 'b3');
    that.house(ctx, 'pink', 280, 120, 20, 110, 'b4');

    ctx.draw();
  },
  onUpdate: function() {
    const ctx = this.data.canvas;
    this.lines(ctx, 320, 320, 20, 20);
    ctx.stroke();
    for (var key in this.data.box) {
      var arr = this.data.box[key];
      ctx.setFillStyle(arr[4]);
      ctx.fillRect(arr[0], arr[1], arr[2], arr[3]);
    }

    for (var key in this.data.frames) {
      var arr = this.data.box[key];
      ctx.setStrokeStyle('black');
      ctx.strokeRect(arr[0], arr[1], arr[2], arr[3]);
    }

    ctx.draw();
  },
  MoveStart: function(e) {
    var arr = e.touches[0];
    var who = this.inside(arr['x'], arr['y']);
    console.log('you touched ' + who);
    this.data.frames = {};
    if (who == '0') {
      return;
    }
    if (e.timeStamp - this.touchStartTime < 300){
      var that = this
      wx.showActionSheet({
        itemList: ['删除'],
        success: function (res) {
          console.log(res.tapIndex)
          delete that.data.box[who];
          delete that.data.frames[who];
          that.onUpdate();
        },
        fail: function (res) {
          console.log(res.errMsg)
        }
      });
      return;
    }
    this.touchStartTime = e.timeStamp;

    var box = this.data.box[who];
    this.data.frames[who] = box;
    this.data.moving['dx'] = arr['x'] - box[0];
    this.data.moving['dy'] = arr['y'] - box[1];
    this.data.moving['state'] = 1;
    this.data.moving['id'] = who;
    this.onUpdate();
  },
  JudgeGestures: function(e) {
    
    var arr = e.touches[0];
    if (this.data.moving['state'] == 1) {
      var who = this.data.moving['id'];
      if (who == '0') {
        return;
      }
      var box = this.data.box[who];
      var dx = this.data.moving['dx'];
      var dy = this.data.moving['dy'];
      box[0] = arr['x'] - dx;
      box[1] = arr['y'] - dy;
      if(box[0] < 0) {
        box[0] = 0
      }
      if(box[0] + box[2] > 320) {
        box[0] = 320 - box[2]
      }

      if(box[1] < 0) {
        box[1] = 0
      }

      if(box[1] + box[3] > 320) {
        box[1] = 320 - box[3]
      }
      this.data.box[who] = box;
      console.log(JSON.stringify(this.data.box))
      this.onUpdate();
    }
  },
  MoveEnd: function(e) {
    this.data.moving['state'] = 0;
    this.data.moving['id'] = '0';
    this.data.moving['dx'] = 0;
    this.data.moving['dy'] = 0;
  },
  lines: function(ctx, xmax, ymax, dx, dy) {
    var startx = 0;
    var starty = 0;
    while (true) {
      startx = 0;
      starty += dy;
      if (starty > ymax) {
        break;
      }
      ctx.setStrokeStyle("#F6F6F6");
      ctx.setLineWidth(1);
      ctx.moveTo(startx, starty);
      ctx.lineTo(xmax, starty);
    }

    while (true) {
      starty = 0;
      startx += dx;
      if (startx > xmax) {
        break;
      }
      ctx.setStrokeStyle("#F6F6F6");
      ctx.setLineWidth(1);
      ctx.moveTo(startx, starty);
      ctx.lineTo(startx, ymax);
    }
  },
  house: function(ctx, color, x, y, dx, dy, id) {
    ctx.setFillStyle(color);
    ctx.fillRect(x, y, dx, dy);
    var arr = [x, y, dx, dy, color];
    this.data.box[id] = arr;
  },
  inside: function(x, y) {
    var id = '0';
    for (var key in this.data.box) {
      var arr = this.data.box[key];
      if (x >= arr[0] && x <= arr[0] + arr[2] && y >= arr[1] && y <= arr[1] + arr[3]) {
        id = key;
        break;
      }
    }
    return id;
  },
  playQuiz(e){
    var index = e.target.dataset.index
    var title = e.target.dataset.title
    wx.navigateTo({
      url: '../quiz/quiz?index=' + index,
    })
  },
  iamvip:function(e) {
    wx.showModal({
      title: '感谢您的支持',
      content: '素朴网联建造师（素造）免费提供复习平台。我们正在考虑开通会员机制，以便提供更优质的服务。',
    })
    wx.request({
      url: head,
      data:{
        todo:'vip'
      },
      success(res) {
        console.log(res.data)
      }
    })
  },
  sys: function() {
    var that = this;
    wx.getSystemInfo({
      success: function(res) {
        that.setData({
          windowW: res.windowWidth,
          windowH: res.windowHeight
        })
      },
    })
  }
});