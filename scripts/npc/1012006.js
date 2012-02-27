function start() {
	cm.getPlayer().getCurrentWeddingList().add(cm.getPlayer().getName());
	cm.sendOk("The players are: " + cm.getPlayer().getCurrentWeddingList());
	cm.dispose();
}