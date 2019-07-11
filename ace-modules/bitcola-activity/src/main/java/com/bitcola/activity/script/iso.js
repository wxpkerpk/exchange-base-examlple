$(function () {
    let pin = '543322';
    let scriptUser = [
        {
            userId:"200016",
            username:'L6DUPEL2358866',
            password:"YHKJ20180823",
            token:""
        },
        {
            userId:"200019",
            username:'momoko',
            password:"YHKJ20180823",
            token:""
        },
        {
            userId:"200021",
            username:'1H9SYG72425936',
            password:"YHKJ20180823",
            token:""
        },
        {
            userId:"200151",
            username:'史蒂芬',
            password:"YHKJ20180823",
            token:""
        },
        {
            userId:"209017",
            username:'L2HHI1NC353543',
            password:"YHKJ20180823",
            token:""
        },
        {
            userId:"209089",
            username:'E8I87MKC112684',
            password:"YHKJ20180823",
            token:""
        },
        {
            userId:"209232",
            username:'X647A9LJ820373',
            password:"YHKJ20180823",
            token:""
        },
        {
            userId:"209251",
            username:'KQODIOFJ987334',
            password:"YHKJ20180823",
            token:""
        },
        {
            userId:"209259",
            username:'OBUECU71615766',
            password:"YHKJ20180823",
            token:""
        },
        {
            userId:"209271",
            username:'3SN7PT9J161651',
            password:"YHKJ20180823",
            token:""
        },
        {
            userId:"209293",
            username:'D8BMNXJJ625835',
            password:"YHKJ20180823",
            token:""
        },
        {
            userId:"209309",
            username:'6LS1G9TI649406',
            password:"YHKJ20180823",
            token:""
        },
        {
            userId:"209617",
            username:'IS9H1JEI137832',
            password:"YHKJ20180823",
            token:""
        },
        {
            userId:"209615",
            username:'X0R8FM53460926',
            password:"YHKJ20180823",
            token:""
        },
        {
            userId:"209610",
            username:'NUJ127ZX738801',
            password:"YHKJ20180823",
            token:""
        },
        {
            userId:"209820",
            username:'SQF9SEHG807752',
            password:"YHKJ20180823",
            token:""
        }
    ];

    let hs = '<td>\n' +
        '            <input type="number" placeholder="下单金额" class="buyNumber">\n' +
        '            <button class="buy">买</button>\n' +
        '        </td>\n' +
        '        <td>\n' +
        '            <input class="amount" type="number" placeholder="总金额">\n' +
        '        </td>\n' +
        '        <td>\n' +
        '            <input class="minSec" type="number" placeholder="最小(s)">\n' +
        '            <input class="maxSec" type="number" placeholder="最大(s)">\n' +
        '        </td>\n' +
        '        <td>\n' +
        '            <input class="time" type="number" placeholder="下单次数">\n' +
        '        </td>\n' +
        '        <td>\n' +
        '            <button  class="startScript">开始</button>\n' +
        '        </td>';

    let getUser = function(userId){
        for (let i = 0; i < scriptUser.length; i++) {
            let user = scriptUser[i];
            if (userId === user.userId) return user;
        }
    };

    let baseUrl = "https://bitcola.top";
    // let baseUrl = "http://112.74.59.207:8765";
    let apiLogin = "/api/auth/jwt/token";
    let apiBuy = "/api/activity/iso/buy";
    let apiRandomSomeRecord = "/api/activity/iso/randomSomeRecord";

    let colaGet = function (api,userId,callback){
        let token = getUser(userId).token;
        if (!token) {
            layer.msg('用户:'+userId+" 未登录")
        }
        $.ajax({
            url: baseUrl + api,
            headers:{
                "Authorization":token,
                "ColaUserAgent":"web",
                "ColaDeviceId":'1'
            },
            method: 'get',
            success: callback
        })
    };
    let colaPost = function (api,userId,body,callback){
        let token = getUser(userId).token;
        if (!token) {
            layer.msg('用户:'+userId+" 未登录")
        }
        $.ajax({
            url: baseUrl + api,
            contentType:'application/json',
            headers:{
                "Authorization":token,
                "ColaUserAgent":"web",
                "ColaDeviceId":'1'
            },
            method: 'post',
            data:JSON.stringify(body),
            success: callback
        })
    };


    // 初始化列表
    let refresh = function (){
        colaGet("/api/activity/iso/rank?type=person",'209271',function (res) {
            $('#dataTable').html('');
            let noRecordUser = [];
            Object.assign(noRecordUser,scriptUser);
            res.data.forEach(function (data) {
                refreshTable(data.userId,data.index,data.number,data.reward);
                for (let i = 0; i < noRecordUser.length; i++) {
                    let user = noRecordUser[i];
                    if (user.userId === data.userId) {
                        noRecordUser.splice(i,1)
                    }
                }
            });
            for (let i = 0; i < noRecordUser.length; i++) {
                let user = noRecordUser[i];
                refreshTable(user.userId,'-','-','-');
            }

        });
    };
    refresh();

    let refreshTable = function (userId,index,number,reward) {
        let color = '#f00';
        if (getUser(userId) == null){
            color = '#000';
        }
        reward = reward?reward:'-';
        let htmlText = $('#dataTable').html();
        htmlText += '<tr>';
        htmlText += '<td class="userId" style="color: '+color+';">'+userId+'</td>';
        htmlText += '<td>'+index+'</td>';
        htmlText += '<td>'+number+'</td>';
        htmlText += '<td>'+reward+'</td>';
        htmlText += hs;
        htmlText += '</tr>';
        $('#dataTable').html(htmlText);
    }


    $('#dataTable').on('click','.buy',function () {
        let row = $(this.closest('tr'));
        let buyNumber = row.find('.buyNumber').val();
        let userId = row.find('.userId').text();
        let body = {
            "amount": buyNumber,
            "pin": pin
        };
        colaPost(apiBuy,userId,body,function (res) {
            layer.msg(res.message);
            // refresh();
        })
    })

    $('#dataTable').on('click','.startScript',function () {
        let row = $(this.closest('tr'));
        // 总数量
        let amount = row.find('.amount').val();
        // 下单次数
        let time = row.find('.time').val();
        // 最小 s
        let minSec = row.find('.minSec').val();
        // 最大 s
        let maxSec = row.find('.maxSec').val();
        // 最小数量
        let number = amount / time;
        let minNumber = number * 0.8;
        // 最大数量
        let maxNumber = number * 1.2;
        // 用户 ID
        let userId = row.find('.userId').text();

        let second = 0;
        layer.msg('正在下单,请不要关闭浏览器!!!!');
        for (let i = 0; i < time; i++) {
            let sec = parseFloat(colaRandom(minSec,maxSec));
            second += sec;
            if (number == 0) return;
            if (sec == 0) return;
            setTimeout(function () {
                let number = colaRandom(minNumber,maxNumber);
                let body = {
                    "amount": number,
                    "pin": pin
                };
                colaPost(apiBuy,userId,body,function (res) {
                    layer.msg(res.message);
                    // refresh();
                })
            },second * 1000)

        }




    });

    let colaRandom = function (minNum,maxNum) {
        minNum = parseFloat(minNum);
        maxNum = parseFloat(maxNum);
        if(minNum!==undefined&&minNum!==undefined){
            return (Math.random()*(maxNum-minNum+1)+minNum).toFixed(4);
        } else if(minNum!==undefined){
            return (Math.random()*minNum+1).toFixed(4);
        } else {
            return 0
        }
    };



    $('#login').on('click',function () {
        for (let i = 0; i < scriptUser.length; i++) {
            let user = scriptUser[i];
            $.ajax({
                url: baseUrl + apiLogin,
                async: false,
                headers:{
                    "ColaUserAgent":"web",
                    "ColaDeviceId":'1'
                },
                method: 'post',
                contentType:'application/json',
                data: JSON.stringify({
                    username:user.username,
                    password:user.password
                }),
                success: function (res) {
                    user.token = res.data;
                    layer.msg(user.userId+' 登录成功');
                }
            })
        }
    })



    $('#randomSomeRecord').on('click',function () {
        let randomNumber = $('#randomNumber').val();
        let randomUserIdMin = $('#randomUserIdMin').val();
        let randomUserIdMax = $('#randomUserIdMax').val();
        let randomAmountMin = $('#randomAmountMin').val();
        let randomAmountMax = $('#randomAmountMax').val();
        let randomSecMin = $('#randomSecMin').val();
        let randomSecMax = $('#randomSecMax').val();

        let sleepToDo = context(randomNumber, randomUserIdMin, randomUserIdMax, randomAmountMin, randomAmountMax, randomSecMin, randomSecMax);
        sleepToDo();
    });

    let randomFunction = function(userId,amount, sec){
        console.log(new Date(), `sec: ${sec}`);
        randomSomeRecord(userId,amount);
    };

    /**
     * 随机小单接口
     * @param userId
     * @param amount
     */
    let randomSomeRecord = function (userId, amount) {
        if (!userId || !amount) {
            //layer.msg('参数错误');
            return;
        }
        console.log(userId+" 下单:"+amount);
        $.get(baseUrl+apiRandomSomeRecord+'?key=kaiqiu&userId='+userId+'&amount='+amount,function (res) {
            console.log(res);
        })
    };

    /** sleep */
    let context = (randomNumber, randomUserIdMin, randomUserIdMax, randomAmountMin, randomAmountMax, randomSecMin, randomSecMax, callback) => {
        return function() {
            if (randomNumber <= 0) return;
          let sec = parseInt(colaRandom(randomSecMin,randomSecMax));
          let userId = parseInt(colaRandom(randomUserIdMin,randomUserIdMax));
          if (userId > 211100 || userId < 209915) return;
          let amount = colaRandom(randomAmountMin,randomAmountMax);
          randomFunction(userId,amount, sec);
          randomNumber --;
          setTimeout(arguments.callee, sec * 1000)
        };
    }



});
