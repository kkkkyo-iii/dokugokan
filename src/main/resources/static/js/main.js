document.addEventListener('DOMContentLoaded', function() {

	// === モーダル（ポップアップ）の制御 ===

	const openBtn = document.getElementById('openModalBtn');
	const closeBtn = document.getElementById('closeModalBtn');
	const modal = document.getElementById('modalOverlay');

	// [ + 投票する ] ボタンがクリックされた時の処理
	if (openBtn) {
		openBtn.addEventListener('click', function() {
			modal.style.display = 'flex';
		});
	}

	// [ × ] ボタンがクリックされた時の処理
	if (closeBtn) {
		closeBtn.addEventListener('click', function() {
			modal.style.display = 'none';
		});
	}

	// モーダルの背景部分がクリックされた時の処理
	if (modal) {
		window.addEventListener('click', function(event) {
			if (event.target === modal) {
				modal.style.display = 'none';
			}
		});
	}

	// 「もっと見る」ボタンの制御
    const showMoreBtns = document.querySelectorAll('.js-show-more-btn');

    showMoreBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetId = btn.getAttribute('data-target');
            const targetElement = document.getElementById(targetId);
            const textSpan = btn.querySelector('span:first-child'); // テキスト部分

            if (targetElement) {

                targetElement.classList.toggle('hidden-tags');

                // 2. ボタンの見た目を変える
                btn.classList.toggle('is-open');

                // 3. テキストの切り替え
                if (btn.classList.contains('is-open')) {
                    textSpan.textContent = '閉じる';
                } else {
                    textSpan.textContent = 'もっと見る';
                }
            }
        });
    });
});