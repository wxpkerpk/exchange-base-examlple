$(function () {
    // let baseUrl = "https://bitcola.top";
    let baseUrl = "http://112.74.59.207:8765";
    let apiRandomSomeRecord = "/api/launchpad/resonance/randomSomeRecord";

    let skrScriptUser = [
        [209915,211100],
        [212527,222526]
    ];

    let hs = '<td>' +
        '<input type="number" placeholder="下单金额" class="buyNumber">' +
        '<button class="buy">买</button>' +
        '</td>' +
        '<td>' +
        '<input class="amount" type="number" placeholder="总金额">' +
        '</td>' +
        '<td>' +
        '<input class="minSec" type="number" placeholder="最小(s)">' +
        '<input class="maxSec" type="number" placeholder="最大(s)">' +
        '</td>' +
        '<td>' +
        '<input class="time" type="number" placeholder="下单次数">' +
        '</td>' +
        '<td>' +
        '<button  class="startScript">开始</button>' +
        '</td>';




    let colaGet = function (api,callback){
        $.ajax({
            url: baseUrl + api,
            headers:{
                "ColaUserAgent":"web",
                "ColaDeviceId":'1'
            },
            method: 'get',
            success: callback
        })
    };


    // 初始化列表
    let refresh = function (){
        colaGet("/api/launchpad/resonance/rank?coinCode=SKR",function (res) {
            $('#dataTable').html('');
            res.data.forEach(function (data) {
                refreshTable(data.userId,data.index,data.number,data.reward);
            });

        });
    };
    refresh();

    let isScriptUser = function(userId){
        for (let i = 0; i < skrScriptUser.length; i++) {
            let minUserId = skrScriptUser[i][0];
            let maxUserId = skrScriptUser[i][1];
            userId = parseInt(userId);
            if (userId > minUserId && userId < maxUserId){
                return true;
            }
        }
        return false;
    };

    let refreshTable = function (userId,index,number,reward) {
        let color = '#f00;';
        if (!isScriptUser(userId)){
            color = '#000;';
        }
        reward = reward?reward:'-';
        let htmlText = $('#dataTable').html();
        htmlText += '<tr style="color: '+color+'">';
        htmlText += '<td class="userId">'+userId+'</td>';
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
        randomSomeRecord(userId,buyNumber)
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
                randomSomeRecord(userId,number)
            },second * 1000)

        }




    });


    $('#randomLargeOrder').on('click',function () {
        let amount = $('#largeOrder').val();
        let user = randomScriptUser();
        randomSomeRecord(user,amount);
    })

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

    let colaRandomInt = function (minInt,maxInt) {
        return parseInt(Math.random()*(maxInt-minInt+1)+minInt);
    };



    $('#randomSomeRecord').on('click',function () {
        let randomNumber = $('#randomNumber').val();
        let randomAmountMin = $('#randomAmountMin').val();
        let randomAmountMax = $('#randomAmountMax').val();
        let randomSecMin = $('#randomSecMin').val();
        let randomSecMax = $('#randomSecMax').val();

        let sleepToDo = context(randomNumber, randomAmountMin, randomAmountMax, randomSecMin, randomSecMax);
        sleepToDo();
    });


    /**
     * 随机小单接口
     * @param userId
     * @param amount
     */
    let randomSomeRecord = function (userId, amount) {
        if (!userId || !amount) {
            layer.msg('参数错误');
            return;
        }
        if (!isScriptUser(userId)){
            layer.msg('只能下单脚本用户');
            return;
        }
        console.log('用户ID:'+userId+" 下单 --> "+amount);
        $.get(baseUrl+apiRandomSomeRecord+'?coinCode=SKR&key=kaiqiu&userId='+userId+'&amount='+amount,function (res) {
            layer.msg(res.message);
        })
    };

    /** sleep */
    let context = (randomNumber,randomAmountMin, randomAmountMax, randomSecMin, randomSecMax, callback) => {
        return function() {
            if (randomNumber <= 0) return;
          let sec = parseInt(colaRandom(randomSecMin,randomSecMax));
          let userId = randomScriptUser();
          let amount = colaRandom(randomAmountMin,randomAmountMax);
            randomSomeRecord(userId,amount);
          randomNumber --;
          setTimeout(arguments.callee, sec * 1000)
        };
    }

    let randomScriptUser = function () {
        let u = parseInt(colaRandomInt(0,1));
        return colaRandomInt(skrScriptUser[u][0],skrScriptUser[u][1]);
    }


});
