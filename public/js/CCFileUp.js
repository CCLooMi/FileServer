//created by chenxianjun at 2019.01.18
(function () {
    File.prototype.fileSizeFormats = {
        'Byte': Math.pow(10, 0),
        'KB': Math.pow(10, 3),
        'MB': Math.pow(10, 6),
        'GB': Math.pow(10, 9),
        'TB': Math.pow(10, 12),
        'PB': Math.pow(10, 15),
        'EB': Math.pow(10, 18),
        'ZB': Math.pow(10, 21),
        'YB': Math.pow(10, 24),
        'BB': Math.pow(10, 27)
    };
    File.prototype.formatSize = function (lenght) {
        var r;
        var l = lenght || 2;
        for (var k in this.fileSizeFormats) {
            var v = this.fileSizeFormats[k];
            r = this.size / v;
            if (r >= 1 && r < 1000) {
                r = parseInt(r * Math.pow(10, l), 10) / Math.pow(10, l) + k;
                break;
            }
        }
        return r;
    };

    let bytesToHex;
    (function () {
        let csm=[];
        (function () {
            var cs="0123456789abcdef";
            var n=0;
            for(var i=0;i<16;i++){
                for(var j=0;j<16;j++,n++){
                    csm[n]=cs[i]+cs[j];
                }
            }
        })();
        bytesToHex=function (a) {
            var s='';
            for(var i=0;i<a.length;i++){
                s+=csm[a[i]&0xff];
            }
            return s;
        }
    })();

    function readFile(file, block) {
        var reader = new FileReader();
        reader.onload = function (e) {
            if (file.worker) {
                file.worker.postMessage({
                    'msg': e.target.result,
                    'block': block
                });
            }
        };
        reader.readAsArrayBuffer(file.slice(block.start, block.end));
    }
    var selfPath=getMyPath();
    function getMyPath(){
    	var ss=document.currentScript||document.querySelector("script[src*='CCFileUp']");
    	if(ss){
    		var s=ss.src;
            return s.substr(0,s.lastIndexOf('/'));
    	}else{
    		return '';
    	}
    }
    function ele2url(id) {
        var ele = document.getElementById(id);
        if(ele){
        	if ('' != ele.innerText.trim()) {
        		if(ele.bsrc){
        			return ele.bsrc;
        		}
                var blob = new Blob([ele.innerText], {type: ele.type});
                ele.bsrc=window.URL.createObjectURL(blob);
                return ele.bsrc;
            } else {
                return ele.src;
            }
        }else{
        	return selfPath+"/md5Worker.min.js";
        }
    }

    function getWorker(id) {
        return new Worker(ele2url(id));
    }

    var btx = new CCBitsyntax("hd:1/I,(hd){0:{id:16/Hex,size:8/I,suffix:/U8,'#',type/U8,'#',name/U8},1:{indexStart:8/I,indexEnd:8/I,completePercent:4/I},2:{data/AB}}",
        {
            fromLib: {},
            toLib: {
                AB: function (buff) {
                    var a = new Uint8Array(buff);
                    var r = [];
                    for (var i = 0; i < a.length; i++) {
                        r[i] = a[i];
                    }
                    return r;
                }
            }
        });

    function readFileByCommand(file, command) {
        var reader = new FileReader();
        reader.onload = function (e) {
            var a = btx.convertToIntArray({hd: 2, data: reader.result});
            file.socket.send(new Blob([new Uint8Array(a)]));
        }
        reader.readAsArrayBuffer(file.slice(command.indexStart, command.indexEnd));
    }

    function readFileHead(file, callback) {
        var reader = new FileReader();
        reader.onload = function (e) {
            file.head = bytesToHex(new Uint8Array(reader.result));
            callback && callback(file);
        }
        reader.readAsArrayBuffer(file.slice(0, 20));
    }

    function createSocket(url) {
        var $this = this;
        this.uploadWSurl = this.uploadWSurl || url || window
            .location
            .href
            .replace(/#\!\/.+/, '')
            .match(/\w+:\/\/[^\/]+\/[^\/]+\/|\w+:\/\/[^\/]+\//)[0]
            .replace('http:', 'ws:')
            .replace('https:', 'wss:') + 'ws';
        try {
            console.log('Connecting socket to [ ' + this.uploadWSurl + ' ]');
            var socket = new WebSocket(this.uploadWSurl);
            socket.onopen = function () {
                $this.socketPool.push(socket);
                console.log('Socket opened.');
            };
            socket.onclose = function () {
                console.clear();
                console.log('Socket closed.');
                if ($this.socketPool.length) {
                    $this.socketPool = [];
                }
                if (socket.file) {
                    $this.filesToUpload.push(socket.file);
                    delete socket.file;
                }
                setTimeout(function () {
                    createSocket.apply($this);
                }, 500);
            }
            return socket;
        } catch (e) {
            console.log(e);
        }
    }

    var CCFileUp = function (uploadWSurl) {
        var $this = this;
        this.bufferSize = 512 * 1024;
        this.workers = 0;
        this.concurent=3;
        this.cacheable=true;
        this.filesToHash=[];
        this.filesToUpload=[];
        this.socketPool=[];
        createSocket.apply(this, [uploadWSurl]);
        setInterval(function () {
            if ($this.workers < $this.concurent &&
                $this.filesToHash &&
                $this.filesToHash.length) {
                $this.startHashFiles();
            }
            if ($this.socketPool.length &&
                $this.filesToUpload.length) {
                $this.startUploadFile();
            }
        }, 500)
    }
    CCFileUp.prototype = {
        doHashWork: function (file, worker) {
            var $this = this;
            file.worker = worker;
            file.step = 1;
            worker.addEventListener('message', function (e) {
                if(file.stopHash){
                    file.worker.terminate();
                    this.hashStopped&&this.hashStopped();
                    $this.hashNexFile();
                    return;
                }
                var d = e.data;
                var block = d.block;
                if (d.firstHash) {
                    file.firstHash = d.firstHash;
                    var fHash = localStorage[d.firstHash];
                    if (fHash) {
                        file.worker.terminate();
                        file.id = fHash;
                        file.progress = 1;
                        file.onProcess && file.onProcess();
                        file.onHashComplete && file.onHashComplete();
                        $this.hashNexFile();
                        return;
                    }
                }
                if (d.result) {
                    file.worker.terminate();
                    file.id = d.result;
                    file.progress = 1;
                    file.onProcess && file.onProcess();
                    file.onHashComplete && file.onHashComplete();
                    if (file.firstHash) {
                        localStorage[file.firstHash] = d.result;
                    }
                    $this.hashNexFile();
                } else {
                    file.progress = block.end / file.size;
                    file.onProcess && file.onProcess();
                    if (block.end != file.size) {
                        block.start += $this.bufferSize;
                        block.end += $this.bufferSize;
                        $this.cacheable&&block.step++;
                        if (block.end >= file.size) {
                            block.end = file.size;
                        }
                        readFile(file, block);
                    }
                }
            });
            readFile(file, {
                fileSize: file.size,
                start: 0,
                end: ($this.bufferSize > file.size ? file.size : $this.bufferSize),
                step: 0
            });
        },
        hashNexFile: function () {
            if (this.filesToHash && this.filesToHash.length) {
                this.doHashWork(this.filesToHash.shift(), getWorker("hashWorker"));
                this.workers++;
            }
        },
        startHashFiles: function () {
            while(this.workers<this.concurent&&
            this.filesToHash && this.filesToHash.length){
                this.hashNexFile();
            }
        },
        uploadFile: function (file, socket) {
            var $this=this;
            file.step=2;
            //save socket in file for recycle
            file.socket = socket;
            /*save file in socket when socket closed or error occur
             then we need put the file into filesToUpload again*/
            socket.file = file;
            socket.onmessage = function (event) {
                if(!file.stopUpload){
                    var reader = new FileReader();
                    reader.onload = function (e) {
                        var command = btx.convertToObject(reader.result);
                        if (command.hd == 1) {
                            file.progress = command.completePercent / 100000000;
                            file.onProcess();
                            readFileByCommand(file, command);
                        } else if (command.hd == 3) {
                            delete file.socket;
                            delete socket.file;
                            file.progress = 1;
                            file.onProcess();
                            file.uploadComplete&&file.uploadComplete();
                            $this.uploadNextFile(socket);
                        }
                    }
                    reader.readAsArrayBuffer(event.data);
                }else{
                    delete file.socket;
                    delete socket.file;
                    this.uploadStopped&&this.uploadStopped();
                    $this.uploadNextFile(socket);
                }
            }
            if (socket.readyState == WebSocket.OPEN) {
                var fi = {
                    hd: 0,
                    id: file.id,
                    size: file.size,
                    suffix: file.suffix,
                    type: file.type,
                    name: file.name
                };
                var a = btx.convertToIntArray(fi);
                socket.send(new Blob([new Uint8Array(a)]));
            }
        },
        uploadNextFile: function (socket) {
            var file = this.filesToUpload.shift();
            if (file) {
                this.uploadFile(file, socket);
            } else {
                //recycle socket
                this.socketPool.push(socket);
            }
        },
        startUploadFile: function () {
            var socket = this.socketPool.shift();
            if (socket) {
                this.uploadNextFile(socket);
            }
        },
        addFiles:function (files) {
            var $this=this;
            for(var i=0;i<files.length;i++){
                files[i].remove=function(){
                    var idx=$this.filesToHash.indexOf(this);
                    if(idx>-1){
                        $this.filesToHash.splice(idx,1);
                    }else if(this.step==1){
                        this.stopHash=true;
                    }else{
                        idx=$this.filesToUpload.indexOf(this);
                        if(idx>-1){
                            $this.filesToUpload.splice(idx,1);
                        }else if(this.step==2){
                            this.stopUpload=true;
                        }
                    }
                }
                files[i].onHashComplete=function(){
                    $this.workers--;
                    this.hashComplete&&this.hashComplete();
                    $this.filesToUpload.push(this);
                };
                readFileHead(files[i],function (file) {
                    file.suffix=file.name.substring(file.name.lastIndexOf('.'));
                    $this.filesToHash.push(file);
                });
            }
        }
    };
    window.CCFileUp = CCFileUp;
})();
