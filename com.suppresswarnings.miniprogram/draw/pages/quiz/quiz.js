// pages/quiz/quiz.js
var touchStartTime = 0
const head = 'https://suppresswarnings.com/like.http?action=draw'
Page({

  /**
   * 页面的初始数据
   */
  data: {
    list:[],
    index:0,
    count:0,
    current:'',
    quiz:{},
    mywrong:0,
    mystar:0,
    option:{'A':'', 'B':'', 'C':'', 'D':''},
    study:false
  }, 
  showrong(e) {
    var wrong = wx.getStorageSync('wrong') || []
    wx.showModal({
      title: '我的错题',
      content: '当前错题总数：' + wrong.length,
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
  choose(e) {
    var chose = e.target.dataset.option
    var quiz = this.data.quiz
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
    if (idx >= that.data.list.length) {
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
    var curr = that.data.list[idx]
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
    wx.request({
      url: head,
      data: {
        todo: 'list',
      },
      success(res) {
        var ret = res.data
        console.log(ret)
        that.setData({
          list:ret.data
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