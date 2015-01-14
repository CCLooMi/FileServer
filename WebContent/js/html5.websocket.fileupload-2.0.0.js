/**
 * Created by xianjun on 2015/1/4.
 */
(function($){
    var FileUploader=function(element,options){
        var that=this;
        this.element=$(element);
        this.container=options.container||'body';
        this.isInput=options.isInput||true;
        this.isInline=true;

        this.concurrentHash=options.concurrentHash||3;
        this.concurrentUpload=options.concurrentUpload||3;
        this.concurrentHashCurrent=0;
        this.wsuri=options.wsuri;
        this.debugMode=options.debugMode||false;

        this.fileInput=$(UPGlobal.fileInput)
            .appendTo(this.container)
            .on({
                change: $.proxy(this.fileSelect,this)
            });
        if(this.debugMode){
            UPGlobal.template+=UPGlobal.consTemplate;
        }
        this.panel=$(UPGlobal.template)
            .appendTo(this.isInline?this.element:this.container)
            .on({
                click: $.proxy(this.click,this)
            });
        this.attachDragEvents();
        this.initWS();
    };
    FileUploader.prototype={
        initWS:function(){
            if('WebSocket' in window){
                for(var i=0;i<this.concurrentUpload;i++){
                    var ws=new WebSocket(this.wsuri);
                    ws.onopen=this.onOpen(this);
                    ws.onmessage=this.onMessage(this);
                    UPGlobal.wsS.push(ws);
                }
            }else if('MozWebSocket' in window){
                for(var i=0;i<this.concurrentUpload;i++){
                    var ws=new MozWebSocket(this.wsuri);
                    ws.onopen=this.onOpen;
                    ws.onmessage=this.onMessage;
                    UPGlobal.wsS.push(ws);
                }
            }else{
                alert('Websocket create error.');
            }
        },
        onOpen:function(that){
            return function(){
                that.log('Websocket is opened.');
            }
        },
        onMessage:function(that){
            return function(e){
                var ws=this;
                var obj=JSON.parse(e.data);
                if(obj.typeId=='uploadCommand'){
                    that.log('文件完成：'+parseInt(obj.completePercent*65+35,10)+'%');
                    $('[name="'+ obj.fileId+'"]').css('width',parseInt(obj.completePercent*65+35,10)+'%');
                    if(obj.completePercent!=1){
                        that.doUploadByCommand(ws,obj);
                    }else{
                        UPGlobal.fileUploadStepMap[UPGlobal.fileIdToFileNameMap[obj.fileId]]=4;//onSuccess
                        that.log('文件上传完成，开始上传下一个文件。');
                        that.uploadNextFile(ws);//该ws完成文件上传，接着继续上传下一个文件
                        $('[name="'+ obj.fileId+'"]').closest('.progress').slideUp(1000).closest('td').css('color','green');
                    }
                }//if end
            }
        },
        hashNextFile:function(){
            if(UPGlobal.filesToUpload.length!=0){
                var f=UPGlobal.filesToUpload.shift();
                var worker=new Worker('js/calculator.worker.fileId.js');
                worker.addEventListener('message', this.handleHashWorkerEvent(f));
                UPGlobal.workers[f.name]=worker;//记录每个文件的worker用于后面清除worker
                this.doHashWork(f,worker);
            }else{
                //Hash计算完毕要恢复此值
                this.concurrentHashCurrent=0;
            }
        },
        startHashFile:function(){
            for(var f,worker;this.concurrentHashCurrent<this.concurrentHash;this.concurrentHashCurrent++){
                if(UPGlobal.filesToUpload.length==0)break;
                f=UPGlobal.filesToUpload.shift();
                worker=new Worker('js/calculator.worker.fileId.js');
                worker.addEventListener('message', this.handleHashWorkerEvent(f));
                UPGlobal.workers[f.name]=worker;//记录每个文件的worker用于后面清除worker
                this.doHashWork(f,worker);
            }
        },
        doHashWork:function(file,worker){
            UPGlobal.fileUploadStepMap[file.name]=1;//文件上传进入计算文件hash状态
            var i, buffer_size, block, reader, blob, handle_hash_block, handle_load_block;

            //发送数据
            handle_load_block = function (event) {
                worker.postMessage({
                    'message': event.target.result,
                    'block': block
                });
            };
            handle_hash_block = function (event) {
                if (block.end !== file.size) {
                    block.start += buffer_size;
                    block.end += buffer_size;

                    if (block.end > file.size) {
                        block.end = file.size;
                    }
                    reader = new FileReader();
                    reader.onload = handle_load_block;
                    blob = file.slice(block.start, block.end);

                    reader.readAsArrayBuffer(blob);
                }
            };
            buffer_size = 512 * 1024 ;
            block = {
                'file_size': file.size,
                'start': 0
            };
            block.end = buffer_size > file.size ? file.size : buffer_size;
            worker.addEventListener('message', handle_hash_block);
            reader = new FileReader();
            reader.onload = handle_load_block;
            blob = file.slice(block.start, block.end);
            reader.readAsArrayBuffer(blob);
        },
        startUploadFile:function(f){
            if(UPGlobal.wsS.length!=0){
                var ws=UPGlobal.wsS.shift();//返回第一个元素，pop（）返回最后一个元素。
                this.sendFileInfo(ws,f);
                this.log('已从ws连接池中取出第一个ws连接，已发送['+ f.name+']文件信息，正在使用的ws连接加1.');
                UPGlobal.filesUploaded.push(f);
            }else{
                UPGlobal.filesHasHashed.push(f);
                UPGlobal.fileUploadStepMap[f.name]=2;//onWaiting
                this.log('ws连接池中没有可用连接，已将['+ f.name+']放入已计算Hash文件池中.')
            }
        },
        uploadNextFile:function(ws){
            if(UPGlobal.filesHasHashed.length!=0){
                var f=UPGlobal.filesHasHashed.shift();
                this.sendFileInfo(ws,f);
                this.log('从已计算Hash文件池中取出文件['+ f.name+']，并发送文件信息.');
                UPGlobal.filesUploaded.push(f);
            }else{//文件上传结束，回收ws连接
                UPGlobal.wsS.push(ws);
                this.log('没有已计算出Hash值的文件可上传，回收ws连接到连接池中.');
            }
        },
        sendFileInfo:function(ws,f){
            var fileInfo={};
            fileInfo.fileName= f.name;
            fileInfo.fileSize= f.size;
            fileInfo.fileInfo= f.type;
            fileInfo.fileId= f.fileId;
            ws.send(JSON.stringify(fileInfo));
            UPGlobal.fileUploadStepMap[f.name]=3;//onUploading
        },
        doUploadByCommand:function(ws,command){
            var fileName=UPGlobal.fileIdToFileNameMap[command.fileId];
            var f=UPGlobal.allFiles[fileName];
            if(f){
                var blob= f.slice(command.indexStart,command.indexEnd);
                var reader=new FileReader();
                reader.onload=function(e){
//            this.log('Sending data: '+blob.size);
                    ws.send(e.target.result);
                };
                reader.readAsArrayBuffer(blob);
            }else{
                this.uploadNextFile(ws);
            }
        },
        handleHashWorkerEvent:function(f){
            var that=this;
            return function (e) {
                if (e.data.result) {
                    this.concurrentHashCurrent--;

                    var fileId= e.data.result;
                    //此处设置到name属性而不是id属性，是因为如果本地有相同fileId时，则进度显示会出问题（不会影响文件上传）。
                    $('[name="'+ f.name+'"]').find('.progress-bar').css('width', '35%').attr('name',fileId);
                    UPGlobal.workers[f.name].terminate();//关闭workers
                    delete UPGlobal.workers[f.name];

                    f['fileId']=fileId;
                    UPGlobal.fileIdToFileNameMap[fileId]= f.name;//将文件对应的fileId存储到map中
                    that.log('文件已算出hash值，开始上传文件：'+ f.name);
                    that.startUploadFile(f);

                    that.hashNextFile();//hash next file
                } else {
                    $('[name="'+ f.name+'"]').find('.progress-bar').css('width', e.data.block.end * 35 / e.data.block.file_size + '%');
                }
            };
        },
        click:function(e){
            var target=$(e.target).closest('button,td,.drop-zone');
            var addfile=this.panel.find('#addfile');
            var removefile=this.panel.find('tbody tr td:nth-last-child(1)');
            var dropZone=this.panel.find('.drop-zone');
            if(target.is(addfile)||target.is(dropZone)){
                $('#files').trigger('click');
            }else if(target.is(removefile)){
                var $this=target;
                var fileName=$this.closest('tr').remove().find('.progress-bar').closest('td').find('span').html();
                this.doRemoveFileWork(fileName);
            }
        },
        doRemoveFileWork:function(fileName){
            var fileStep=UPGlobal.fileUploadStepMap[fileName];
            switch (fileStep){
                case 0:
                    //alert('nothing');
                    //delete file from filesToUpload
                    this.removeFileFromArray(fileName,UPGlobal.filesToUpload);
                    break;
                case 1:
                    //alert('onHashing');
                    UPGlobal.workers[fileName].terminate();
                    //delete file from filesHasHashed ??不需要，这个时候还没有放入filesHasHashed中
                    this.concurrentHashCurrent--;
                    this.removeFileFromArray(fileName);
                    this.hashNextFile();
                    break;
                case 2:
                    //alert('onWaiting');
                    //delete file from filesHasHashed
                    this.removeFileFromArray(fileName,UPGlobal.filesHasHashed);
                    break;
                case 3:
                    //alert('onUploading');
                    //delete file from filesUploaded ??不需要，这个时候还没有放入fileHasHashed中
                    this.removeFileFromArray(fileName);//移除之后通过command上传将找不到file而终止文件上传。并自动开始上传下一个文件
                    break;
                case 4:
                    //alert('onSuccess');
                    //delete file from filesUploaded
                    this.removeFileFromArray(fileName,UPGlobal.filesUploaded);
                    break;
            }
        },
        removeFileFromArray:function(fileName,filesArray){
            delete UPGlobal.allFiles[fileName];
            if(filesArray){
                for(var i=0;i<filesArray.length;i++){
                    if(fileName===filesArray[i].name){
                        delete filesArray[i];
                        break;
                    }
                }
            }
        },
        attachDragEvents:function(){
            var tablepanel=document.getElementsByClassName('tablepanel');
            tablepanel[0].addEventListener('dragover', $.proxy(this.dragOver,this));
            tablepanel[0].addEventListener('drop', $.proxy(this.fileSelect,this));
        },
        dragOver:function(e){
            e.stopPropagation();
            e.preventDefault();
        },
        fileSelect:function(e){
            e.stopPropagation();
            e.preventDefault();
            this.hideDropzone();
            var files= e.dataTransfer ? e.dataTransfer.files : e.target.files;
            var output=[];
            for(var i=0,f;f=files[i];i++){
                if(!UPGlobal.allFiles[f.name]){
                    var process='<div name="'+ f.name+'" class="progress"><div class="progress-bar progress-bar-striped active" aria-valuemin="0" aria-valuemax="100" style="width: 0%"></div></div>';
                    output.push('<tr><td><span>'+ f.name+'</span>('+this.formatFileSize(f.size)+')'+process+'</td><td><span class="glyphicon glyphicon-remove"></span></td></tr>');
                    UPGlobal.allFiles[f.name]= f;
                    UPGlobal.filesToUpload.push(f);
                    UPGlobal.fileUploadStepMap[f.name]=0;
                }
            }
            this.panel.find('tbody').append(output.join(''));
            this.startHashFile();//计算hash
        },
        formatFileSize:function(fileSize,lenght){
            var r;
            var l=lenght||2;
            for(var k in UPGlobal.fileSizeFormats){
                var v=UPGlobal.fileSizeFormats[k];
                r=fileSize/v;
                if(r>=1&&r<1000){
                    r=parseInt(r*Math.pow(10,l),10)/Math.pow(10,l)+k;
                    break;
                }
            }
            return r;
        },
        hideDropzone:function(){
            $('.drop-zone').slideUp(500);
        },
        log:function(message){
            if(this.debugMode){
                var console = document.getElementById('console');
                var p = document.createElement('p');
                p.style.wordWrap = 'break-word';
                p.appendChild(document.createTextNode(message));
                console.appendChild(p);
                while (console.childNodes.length > 50) {
                    console.removeChild(console.firstChild);
                }
                console.scrollTop = console.scrollHeight;
            }
        }
    };
    $.fn.fileuploader=function(option){
        var args=Array.apply(null,arguments);
        args.shift();
        var internal_return;
        this.each(function(){
            var $this=$(this),
                data=$this.data('fileuploader'),
                options=typeof option=='object'&&option;
            if(!data){
                $this.data('fileuploader',(data=new FileUploader(this,$.extend({}, $.fn.fileuploader.defaults, options))));
            }
            if(typeof option=='string'&&typeof data[option]=='function'){
                internal_return=data[option].apply(data,args);
                if(internal_return!==undefined){
                    return false;
                }
            }
        });
        if(internal_return!=undefined){
            return internal_return;
        }else{
            return this;
        }
    };
    $.fn.fileuploader.defaults={};
    $.fn.fileuploader.Constructor=FileUploader;

    var UPGlobal={
        allFiles:{},
        filesToUpload:[],
        filesHasHashed:[],
        filesUploaded:[],
        fileIdToFileNameMap:{},
        fileUploadStepMap:{},//0,nothing;1,onHashing;2,onWaiting;3,onUploading;4,onSuccess
        workers:{},
        wsS:[],
        fileSizeFormats:{
            'Byte':Math.pow(10,0),
            'KB':Math.pow(10,3),
            'MB':Math.pow(10,6),
            'GB':Math.pow(10,9),
            'TB':Math.pow(10,12),
            'PB':Math.pow(10,15),
            'EB':Math.pow(10,18),
            'ZB':Math.pow(10,21),
            'YB':Math.pow(10,24),
            'BB':Math.pow(10,27)
        },
        headTemplate:'<div class="panel-heading"><h4>HTML5 fileupload</h4></div>',
        contTemplate:'<div class="tablepanel panel-body"><table class="table table-hover"><tbody></tbody></table>' +
        '<div class="drop-zone"><h1>Drop files here or click for select</h1>将文件拖放到这里或点击这里</div></div>',
        footTemplate:'<div class="panel-footer"><div class="btn-group">' +
        '<button class="btn btn-default btn-danger" id="addfile"><span class="glyphicon glyphicon-plus"></span>添加</button>' +
        '</div></div>',
        consTemplate:'<div id="console" class="well"></div>',
        finpTemplate:'<input id="files" type="file" multiple style="display: none"/>'
    };
    UPGlobal.template='<div class="panel panel-default panel-info">'+
    UPGlobal.headTemplate+
    UPGlobal.contTemplate+
    UPGlobal.footTemplate+
    '</div>';
    UPGlobal.fileInput=UPGlobal.finpTemplate;
    $.fn.fileuploader.UPGlobal=UPGlobal;
})(jQuery);