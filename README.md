## Changelog
1. 增加了docker build， 部分dependencies更新
2. 暂时屏蔽了自动检测更新的功能
3. Rfid扫描功能集成到单独的模块（方便后续调试，加东西）
4. Rfid扫描变成长按trigger扫，松开trigger停
5. OBverify取消了预先下载的方式，回到了边扫边和数据库交互的形式
6. 100ms和uhfservice交互一次（读取epc），读取到的epc表缓存到每300ms和数据库交互一次
7. 美化了UI（布局和图标）
8. RFID Binding的界面增加了详细显示
9. 增加了debug scan 的功能（目前只能先看所有扫到的epc）
10. inbound verification暂时是placeholder，拷贝outbound verification的应用

## Known Issues
1. 安装包进度条（弹窗）看起来和原版冲突，但不影响
2. 寻物的声音提示的频次暂时还没改

**Run the build script**:
   ```bash
	wsl ./build-apk.sh
   ```
   if the build script fail to run, test with "file build-apk.sh, if see a "with CRLF line terminators", run the following script
   sed -i 's/\r$//' build-apk.sh

