// pages/quiz/quiz.js
var touchStartTime = 0
var app = getApp()
const head = 'https://suppresswarnings.com/like.http?action=draw'
Page({

  /**
   * 页面的初始数据
   */
  data: {
    start: {},
    left: 100,
    top: 0,
    list:[],
    todo:[],
    index:0,
    count:0,
    current:'',
    quiz: null,
    mywrong:0,
    mystar:0,
    chose: 0,
    select:0,
    categories: ['一注材料', '一注物理', '一注场地', '一注经济', '一注结构', '一注建知'],
    chapters: ['全部章节', '第一章', '第二章', '第三章', '第四章', '第五章', '第六章', '第七章', '第八章', '第九章', '第十章', '第十一章', '第十二章', '第十三章', '第十四章', '第十五章', '第十六章', '第十七章', '第十八章', '第十九章', '第二十章'],
    option:{'A':'', 'B':'', 'C':'', 'D':''},
    study:false,
    userInfo: null,
    hasUserInfo: false,
    canIUse: wx.canIUse('button.open-type.getUserInfo')
  }, 
  bindPickerChange(e){
    var chose = e.detail.value
    var todo = []
    var list = this.data.list
    var start = this.data.select + '.' + chose
    for(var i =0;i<list.length;i++) {
      if (list[i].startsWith(start)) {
        todo.push(list[i])
      }
    }
    console.log(todo.length)
    this.setData({
      chose: chose,
      todo: todo,
      quiz: null,
      index:0
    })
  },
  navigateBack(e){
    wx.navigateBack()
  },
  getUserInfo: function (e) {
    console.log(e)
    app.globalData.userInfo = e.detail.userInfo
    this.setData({
      userInfo: e.detail.userInfo,
      hasUserInfo: true
    })
  },
  showrong(e) {
    var wrong = wx.getStorageSync('wrong') || []
    wx.showModal({
      title: '我的错题',
      content: JSON.stringify(wrong),
    })
  }, 
  showstar(e){
    var wrong = wx.getStorageSync('star') || []
    wx.showModal({
      title: '我的标星',
      content: JSON.stringify(wrong),
    })
  },
  starquiz(e) {
    if (e.timeStamp - this.touchStartTime < 300) {
      var quiz = this.data.quiz
      var star = wx.getStorageSync('star') || []
      var now = []
      for (var i in star) {
        var q = star[i]
        if(q == undefined) {
          continue
        }
        if(q.id === quiz.id) {
          console.log('delete .... ')
          delete star[i]
        } else {
          now.push(q)
        }
      }
      now.unshift(quiz)
      wx.setStorageSync('star', now)
      this.setData({
        mystar: now.length
      })
      wx.showToast({
        title: '双击标星',
      })
    }
    this.touchStartTime = e.timeStamp
  },
  MoveStart: function (e) {
    var arr = e.touches[0];
    this.setData({
      start: arr
    })
  },
  JudgeGestures: function (e) {
    var start = this.data.start
    var endPoint = e.touches[e.touches.length - 1]
    var translateX = endPoint.clientX - start.clientX
    var translateY = endPoint.clientY - start.clientY
    start = endPoint
    var top = this.data.top + translateY
    var left = this.data.left + translateX
    this.setData({
      start: start,
      left: left,
      top: top
    })
  },
  MoveEnd: function (e) {

  },
  choose(e) {
    var chose = e.target.dataset.option
    var quiz = this.data.quiz
    var user = this.data.userInfo
    if(quiz == null) {
      if (user == null) {
        wx.showToast({
          title: '请登录',
        })
      } else {
        wx.showToast({
          title: '请点击下一题',
        })
      }
      return
    }
    var other = ''
    var option = { 'A': '', 'B': '', 'C': '', 'D': '' }
    quiz.chose = true
    if(chose === quiz.right) {
      option[chose] = 'right'
    } else {
      option[chose] = 'wrong'
      option[quiz.right] = 'right'
      quiz.chose = chose
      var wrong = wx.getStorageSync('wrong') || []
      var now = []
      for (var i in wrong) {
        var q = wrong[i]
        if (q == undefined) {
          continue
        }
        if (q.id === quiz.id) {
          delete wrong[i]
        } else {
          now.push(q)
        }
      }
      now.unshift(quiz)
      wx.setStorageSync('wrong', now)
      wx.showToast({
        title: '错题收藏',
      })
      this.setData({
        mywrong:now.length
      })
    }
    this.setData({
      quiz: quiz,
      option: option
    })
  },
  changemode(e) {
    var study = !this.data.study
    this.setData({
      study:study
    })
    var quiz = this.data.quiz
    var option = { 'A': '', 'B': '', 'C': '', 'D': '' }
    if(study) {
      quiz.chose = true
      option[quiz.right] = 'right'
    } else {
      quiz.chose = false
    }
    this.setData({
      quiz:quiz,
      option: option
    })
  },
  nextquiz(e){
    var that = this
    var option = { 'A': '', 'B': '', 'C': '', 'D': '' }
    that.setData({
      option: option
    })
    var study = that.data.study
    var idx = that.data.index
    if (idx >= that.data.todo.length) {
      wx.showModal({
        title: '题目做完了',
        content: '请问需要重新开始吗？',
        success(res) {
          if(res.confirm) {
            idx = 0
            that.setData({
              index: idx
            })
          }
        }
      })
      return
    }
    var curr = that.data.todo[idx]
    idx += 1
    wx.showLoading({
      title: '加载中...',
    })
    wx.request({
      url: head,
      data:{
        todo:'select',
        id:curr
      },
      success(res) {
        var quiz = res.data.data
        console.log(quiz)
        if(quiz.right == undefined) {
          quiz.right = ''
        }
        quiz.right = quiz.right.trim()
        that.setData({
          quiz: quiz,
          index: idx,
          current: curr
        })
        if(study) {
          var option = { 'A': '', 'B': '', 'C': '', 'D': '' }
          option[quiz.right] = 'right'
          quiz.chose = true
          that.setData({
            quiz: quiz,
            option: option
          })
        }
      },
      complete(res) {
        wx.hideLoading()
      }
    })
  },
  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    var that = this
    wx.showLoading({
      title: '正在加载题库',
    })
    var select = options.index
    var category = this.data.categories[select]
    this.setData({
      category: category,
      select: select
    })
    wx.request({
      url: head,
      data: {
        todo: 'list',
        category: select,
      },
      success(res) {
        var ret = res.data
        var list = ret.data
        if(list.length < 1) {
          that.setData({
            none: true
          })
        } else {
          var last = list[list.length-1]
          console.log(last)
          var chapterid = last.split('\.')[1]
          var max = parseInt(chapterid) + 2
          var chapters = that.data.chapters
          if(max > chapters.length) {
            max = chapters.length
          }
          var chosed = []
          for(var i=0;i<max;i++) {
            var one = chapters[i]
            chosed.push(one)
          }
          console.log(chosed)
          that.setData({
            chapters: chosed
          })
        }
        that.setData({
          list: list,
          todo: list
        })
      },
      complete(res) {
        wx.hideLoading()
      }
    })

    var wrong = wx.getStorageSync('wrong') || []
    var star = wx.getStorageSync('star') || []
    this.setData({
      mywrong:wrong.length,
      mystar: star.length
    })
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function () {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide: function () {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function () {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function () {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function () {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage: function () {

  }
})