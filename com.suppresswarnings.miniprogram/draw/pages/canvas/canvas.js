import CanvasDrag from '../../components/canvas-drag/canvas-drag';

Page({

  /**
   * 页面的初始数据
   */
  data: {
    graph: {},
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function(options) {

  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function() {

  },


  /**
   * 用户点击右上角分享
   */
  onShareAppMessage: function() {

  },

  /**
   * 添加测试图片
   */
  onAddTest() {
    this.setData({
      graph: {
        w: 120,
        h: 120,
        type: 'image',
        url: '../../assets/images/test.jpg',
      }
    });
  },

  /**
   * 添加文本
   */
  onAddText() {
    this.setData({
      graph: {
        type: 'text',
        text: 'helloworld',
      }
    });
  },

  /**
   * 添加矩形
   */
  onAddRect() {
    this.setData({
      graph: {
        type: 'rect',
        color: 'red',
        x:22,
        y:28,
        w:82,
        h:36
      }
    });
  },

  /**
   * 导出图片
   */
  onExport() {
    CanvasDrag.export()
      .then((filePath) => {
        console.log(filePath);
        wx.previewImage({
          urls: [filePath]
        })
      })
      .catch((e) => {
        console.error(e);
      })
  },

  /**
   * 改变文字颜色
   */
  onChangeColor() {
    CanvasDrag.changFontColor('blue');
  },

  /**
   * 改变背景颜色
   */
  onChangeBgColor() {
    CanvasDrag.changeBgColor('yellow');
  },

  /**
   * 改变背景照片
   */
  onChangeBgImage() {
    CanvasDrag.changeBgImage('../../assets/images/test.jpg');
  },

  /**
   * 导出当前画布为模板
   */
  onExportJSON() {
    CanvasDrag.exportJson()
      .then((imgArr) => {
        console.log(JSON.stringify(imgArr));
      })
      .catch((e) => {
        console.error(e);
      });
  },

  onImport() {

    // 无背景
    let temp_theme = [{
      "type": "image",
      "url": "../../assets/images/test.jpg",
      "y": 103,
      "x": 91,
      "w": 120,
      "h": 120,
      "rotate": 0,
      "sourceId": null
    }, {
      "type": "text",
      "text": "helloworld",
      "color": "blue",
      "fontSize": 20,
      "y": 243,
      "x": 97,
      "rotate": 0
    },
      {
        "type": "rect",
        "color": "red",
        "y": 243,
        "x": 97,
        "rotate": 0
      }];

    CanvasDrag.initByArr(temp_theme);
  },

  /**
   * 清空画布
   */
  onClearCanvas: function(event) {
    let _this = this;
    _this.setData({
      canvasBg: null
    });
    CanvasDrag.clearCanvas();
  },
})