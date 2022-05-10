--  
--  This file is part of the OpenLink Software Virtuoso Open-Source (VOS)
--  project.
--  
--  Copyright (C) 1998-2021 OpenLink Software
--  
--  This project is free software; you can redistribute it and/or modify it
--  under the terms of the GNU General Public License as published by the
--  Free Software Foundation; only version 2 of the License, dated June 1991.
--  
--  This program is distributed in the hope that it will be useful, but
--  WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
--  General Public License for more details.
--  
--  You should have received a copy of the GNU General Public License along
--  with this program; if not, write to the Free Software Foundation, Inc.,
--  51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
--  
--  
create procedure tpcd..supplier_add_random(in SF integer, in nNumRows integer) {
	
	declare _strHelper varchar;
	declare _nHelper1, _nHelper2, _nHelper3 integer;

	_nHelper2 := 0;
	while (_nHelper2 < (5 * SF)) {
		_nHelper3 := randomNumber(1, nNumRows);
		declare cr1 cursor for 
			select s_comment from tpcd..supplier where s_suppkey = _nHelper3;
		
		open cr1;
		fetch cr1 into _strHelper;

		update tpcd..supplier set s_comment = 'CustomerComplaints' where current of cr1;
		_nHelper2 := _nHelper2 + 1;
		close cr1;

	}

	_nHelper2 := 0;
	while (_nHelper2 < (5 * SF)) {
		_nHelper3 := randomNumber(1, nNumRows);
		declare cr2 cursor for 
			select s_comment from tpcd..supplier where s_suppkey = _nHelper3;
		
		open cr2;
		fetch cr2 into _strHelper;

		update tpcd..supplier set s_comment = 'CustomerRecommends' where current of cr2;
		_nHelper2 := _nHelper2 + 1;
		close cr2;
	}
}
